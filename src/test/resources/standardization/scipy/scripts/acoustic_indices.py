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


def compute_ACI(spectro, fs, nb_windows, flim=None):
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

    spectrumTemporalSize = spectro.shape[1]
    times = np.array([
        (int(spectrumTemporalSize / nb_windows * (j-1) ),
        int(spectrumTemporalSize / nb_windows * j)-1)
        for j in range(1, nb_windows+1)
    ])

    if flim is not None:
        flim = 2 * flim * spectro.shape[0] / fs
        # print("flim " + str(flim) + " interval " + str(flim[1] - flim[0]))
        # print("nfft {}".format(spectro.shape[0]))
        spectro = spectro[int(flim[0]): int(flim[1]), :]
        # print("cut spectro shape {}".format(spectro.shape))


    jspecs = [np.array(spectro[:, i[0]: i[1] + 1]) for i in times]

    # list of ACI values on each jspecs
    aci = [
        np.sum(np.sum(abs(np.diff(jspec)), axis=1)
        / np.sum(jspec, axis=1)) for jspec in jspecs
    ]

    main_value = np.sum(aci)
    temporal_values = aci

    return main_value, temporal_values

if __name__ == '__main__':
    ########################## SPECTRUM A
    signal = np.arange(70)
    fs = 100.0
    windowSize = 8

    spectrum = scipy.signal.stft(
        x=signal, fs=fs, window='boxcar', noverlap=0,
        nperseg=windowSize, nfft=windowSize, detrend=False,
        return_onesided=True, boundary=None,
        padded=False, axis=-1)[-1]

    amplitudeSpectrum = abs(spectrum)

    np.set_printoptions(threshold=sys.maxsize)
    np.set_printoptions(precision=16)
    f = open("/tmp/fftA.json", "w")
    f.write(json.dumps(formatComplexResults(spectrum).tolist()))
    f.close()

    print("3 win spectrum A")
    aci, temp_val = compute_ACI(amplitudeSpectrum, fs, nb_windows=3, flim=None)
    print(temp_val)
    print(aci)
    print("\n")

    print("4 win spectrum A")
    aci, temp_val = compute_ACI(amplitudeSpectrum, fs, nb_windows=4, flim=None)
    print(temp_val)
    print(aci)
    print("\n")

    ########################## SPECTRUM B
    np.random.seed(0)
    signal = np.arange(256) + np.random.normal(0.1, 1, size=256)
    fs = 100.0
    windowSize = 16

    spectrum = scipy.signal.stft(
        x=signal, fs=fs, window='boxcar', noverlap=0,
        nperseg=windowSize, nfft=windowSize, detrend=False,
        return_onesided=True, boundary=None,
        padded=False, axis=-1)[-1]


    amplitudeSpectrum = abs(spectrum)

    f = open("/tmp/fftB.json", "w")
    f.write(json.dumps(formatComplexResults(spectrum).tolist()))
    f.close()

    print("5 win spectrum B")
    aci, temp_val = compute_ACI(amplitudeSpectrum, fs, nb_windows=5, flim=None)
    print(temp_val)
    print(aci)
    print("\n")


    print("8 win spectrum B")
    aci, temp_val = compute_ACI(amplitudeSpectrum, fs, nb_windows=8, flim=None)
    print(temp_val)
    print(aci)
    print("\n")


    print("4 win fl 10-40 spectrum B")
    aci, temp_val = compute_ACI(amplitudeSpectrum, fs, nb_windows=4, flim=np.array([10, 40]))
    print(temp_val)
    print(aci)
    print("\n")


    print("8 win fl 10-40 spectrum B")
    aci, temp_val = compute_ACI(amplitudeSpectrum, fs, nb_windows=8, flim=np.array([10, 40]))
    print(temp_val)
    print(aci)
    print("\n")

    print("8 win fl 12.24, 31.424 spectrum B")
    aci, temp_val = compute_ACI(amplitudeSpectrum, fs, nb_windows=8, flim=np.array([12.24, 31.424]))
    print(temp_val)
    print(aci)
    print("\n")

    ########################## SPECTRUM C
    np.random.seed(0)
    signal = np.arange(64) + np.random.normal(0.1, 1, size=64)
    fs = 100.0
    windowSize = 15
    nbWind = 2

    spectrum = scipy.signal.stft(
        x=signal, fs=fs, window='boxcar', noverlap=0,
        nperseg=windowSize, nfft=windowSize, detrend=False,
        return_onesided=True, boundary=None,
        padded=False, axis=-1)[-1]


    amplitudeSpectrum = abs(spectrum)

    f = open("/tmp/fftC.json", "w")
    f.write(json.dumps(formatComplexResults(spectrum).tolist()))
    f.close()

    print("2 win fl 12.24, 31.424 spectrum C")
    aci, temp_val = compute_ACI(amplitudeSpectrum, fs, nb_windows=2, flim=np.array([12.24, 31.424]))
    print(temp_val)
    print(aci)
    print("\n")
