#!/usr/bin/env python3
# -*- coding: utf-8 -*-

# Copyright (C) 2017-2018 Project-ODE
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.

# Authors: Paul Nguyen HD


import sys
import numpy as np
from pylab import *
import scipy.signal
from SoundHandler import SoundHandler
import scipy.signal
import json


def compute_spectrogram(
    signal,
    fs,
    windowLength=512,
    windowHop=256,
    square=True,
    windowType='hamming',
    centered=False,
    normalized=True
):
    """
        From https://github.com/patriceguyot/Acoustic_Indices and modified to
        match spectro from seewave R package.
        Compute a spectrogram of an audio signal.
        Return a list of list of values as the spectrogram,
        and a list of frequencies.

        Keyword arguments:
        file -- the real part (default 0.0)

        Parameters:
        file: an instance of the AudioFile class.
        windowLength: length of the fft window (in samples)
        windowHop: hop size of the fft window (in samples)
        scale_audio: if set as True, the signal samples are scale between
            -1 and 1 (as the audio convention). If false the signal samples
            remains Integers (as output from scipy.io.wavfile)
        square: if set as True, the spectrogram is computed as the square of
            the magnitude of the fft. If not, it is the magnitude of the fft.
        hamming: if set as True, the spectrogram use a correlation
            with a hamming window.
        centered: if set as true, each resulting fft is centered on the
            corresponding sliding window
        normalized: if set as true, divide all values by the maximum value
    """

    niquist = fs / 2
    W = scipy.signal.get_window(windowType, windowLength, fftbins=False)

    if centered:
        time_shift = int(windowLength / 2)
        times = range(time_shift, len(signal) + 1 - time_shift, windowHop)  # centered
        frames = [signal[i - time_shift:i + time_shift] * W for i in times]  # centered frames
    else:
        times = np.arange(0, len(signal) - windowLength, windowHop)
        frames = [signal[int(i):int(i) + windowLength] * W for i in times]

    if square:
        spectro = [abs(np.fft.fft(frame, windowLength))[0:windowLength // 2] ** 2 for frame in frames]
    else:
        spectro = [abs(np.fft.fft(frame, windowLength))[1:windowLength // 2 + 1] for frame in frames]

    # set the spectro in a friendly way
    spectro = np.transpose(spectro)

    if normalized:
        # set the maximum value to 1 y
        spectro = spectro / np.max(spectro)

    # vector of frequency<-bin in the spectrogram
    frequencies = [
        i * niquist / float(windowLength / 2) for i in range(windowLength // 2)
    ]

    return spectro, frequencies


def compute_ACI(spectro, wl, f, nb_windows, flim=None):
    """
        From https://github.com/patriceguyot/Acoustic_Indices and modified to
        match ACI from seewave R package
        Compute the Acoustic Complexity Index from the spectrogram
        of an audio signal.

        Reference: Pieretti N, Farina A, Morri FD (2011) A new methodology
        to infer the singing activity of an avian community: the
        Acoustic Complexity Index (ACI). Ecological Indicators, 11, 868-873.

        Ported from the soundecology R package.

        spectro: the spectrogram of the audio signal
        j_bin: temporal size of the frame (in samples)
        flim: freq bounds of freq band to analyze in kHz
    """

    l = spectro.shape[1]
    times = np.array([
        (int(l / nb_windows * (j-1) ), int(l / nb_windows * j)-1)
        for j in range(1, nb_windows+1)
    ])

    print(times)

    if flim is not None:
        flim = flim * 1000 * wl/f
        spectro = spectro[int(flim[0]): int(flim[1]), :]

    # sub-spectros of temporal size j
    jspecs = [np.array(spectro[:, i[0]: i[1] + 1]) for i in times]
    print(jspecs)

    # list of ACI values on each jspecs
    aci = [
        np.sum(np.sum(abs(np.diff(jspec)), axis=1)
        / np.sum(jspec, axis=1)) for jspec in jspecs
    ]

    main_value = np.sum(aci)
    temporal_values = aci

    return main_value, temporal_values

def formatComplexResults(value):
    """
    Results containing complex values are reformatted following
    the same convention as in FeatureEngine, ie:
    [z_0, z_1, ... , z_n] => [Re(z_0), Im(z_0), Re(z_1), ... Im(z_n)]
    """
    initialShape = value.shape

    nSeg = initialShape[1]
    segLength = initialShape[0]

    valueAsScalaFormat = np.zeros((nSeg, 2*segLength), dtype=float)
    valueAsComplex = value.transpose()

    for i in range(nSeg):
        valueAsScalaFormat[i, ::2] = valueAsComplex[i].real
        valueAsScalaFormat[i, 1::2] = valueAsComplex[i].imag

    return valueAsScalaFormat

if __name__ == '__main__':
    signal = np.arange(16 * 16 + 1)
    fs = 100.0

    wl = 16
    nbWind = 3

    fFFT, vFFT = scipy.signal.stft(
        x=signal, fs=fs, window='boxcar', noverlap=0,
        nperseg=wl, nfft=wl, detrend=False,
        return_onesided=True, boundary=None,
        padded=False, axis=-1)[::2]

    spec = abs(vFFT)
    print(len(spec))

    np.set_printoptions(threshold=sys.maxsize)
    np.set_printoptions(precision=16)
    # f = open("/tmp/fftA.json", "w")
    # f.write(json.dumps(formatComplexResults(vFFT).tolist()))
    # f.close()
    # print(formatComplexResults(vFFT))

    fl = np.array([0.03, 0.08])
    aci, temp_val = compute_ACI(spec, wl, fs, nb_windows= nbWind, flim=fl)
    print("flim " + str(fl * 1000 * wl/fs))

    print(aci)
    print(temp_val)
