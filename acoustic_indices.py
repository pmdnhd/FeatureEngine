# -*- coding: utf-8 -*-
"""
@Filename: acoustic_indices
@Date: 07/06/2019
@Project: ImageSeg
@Author: paul
@Notes:
@Usages:
"""

import soundfile as sf
import numpy as np
from pylab import *
from scipy import signal


def compute_spectrogram(sig, fs, windowLength=512, windowHop=256, square=True, windowType='hamming', centered=False,
                        normalized=True):
    """
    From https://github.com/patriceguyot/Acoustic_Indices and modified to match sspectro from seewave R package
    Compute a spectrogram of an audio signal.
    Return a list of list of values as the spectrogram, and a list of frequencies.

    Keyword arguments:
    file -- the real part (default 0.0)

    Parameters:
    file: an instance of the AudioFile class.
    windowLength: length of the fft window (in samples)
    windowHop: hop size of the fft window (in samples)
    scale_audio: if set as True, the signal samples are scale between -1 and 1 (as the audio convention). If false the signal samples remains Integers (as output from scipy.io.wavfile)
    square: if set as True, the spectrogram is computed as the square of the magnitude of the fft. If not, it is the magnitude of the fft.
    hamming: if set as True, the spectrogram use a correlation with a hamming window.
    centered: if set as true, each resulting fft is centered on the corresponding sliding window
    normalized: if set as true, divide all values by the maximum value
    """

    niquist = fs / 2
    W = signal.get_window(windowType, windowLength, fftbins=False)

    if centered:
        time_shift = int(windowLength / 2)
        times = range(time_shift, len(sig) + 1 - time_shift, windowHop)  # centered
        frames = [sig[i - time_shift:i + time_shift] * W for i in times]  # centered frames
    else:
        times = np.arange(0, len(sig) - windowLength, windowHop)
        frames = [sig[int(i):int(i) + windowLength] * W for i in times]

    if square:
        spectro = [abs(np.fft.fft(frame, windowLength))[0:windowLength // 2] ** 2 for frame in frames]
    else:
        spectro = [abs(np.fft.fft(frame, windowLength))[1:windowLength // 2 + 1] for frame in frames]

    spectro = np.transpose(spectro)  # set the spectro in a friendly way

    if normalized:
        spectro = spectro / np.max(spectro)  # set the maximum value to 1 y

    frequencies = [e * niquist / float(windowLength / 2) for e in
                   range(windowLength // 2)]  # vector of frequency<-bin in the spectrogram
    return spectro, frequencies


# -----------------------------------------------------------------------------
def compute_ACI(spectro, wl, f, nb_windows, flim=None):
    """
    From https://github.com/patriceguyot/Acoustic_Indices and modified to match ACI from seewave R package
    Compute the Acoustic Complexity Index from the spectrogram of an audio signal.

    Reference: Pieretti N, Farina A, Morri FD (2011) A new methodology to infer the singing activity of an avian community: the Acoustic Complexity Index (ACI). Ecological Indicators, 11, 868-873.

    Ported from the soundecology R package.

    spectro: the spectrogram of the audio signal
    j_bin: temporal size of the frame (in samples)
    flim: freq bounds of freq band to analyze in kHz


    """

    l = spectro.shape[1]
    times = np.array([(int(l / nb_windows * (j-1) + 1), int(l / nb_windows * j)) for j in range(1, nb_windows+1)]) - 1
    # times2 = [(i[0], i[1]) for i in times]
    # times3 = [i[1] - i[0] for i in times]

    if flim is not None:
        flim = flim * 1000 * wl/f
        spectro = spectro[int(flim[0]): int(flim[1]), :]

    jspecs = [np.array(spectro[:, i[0]: i[1] + 1]) for i in times]  # sub-spectros of temporal size j

    aci = [np.sum(np.sum(abs(np.diff(jspec)), axis=1) / np.sum(jspec, axis=1)) for jspec in
           jspecs]  # list of ACI values on each jspecs

    main_value = np.sum(aci)
    temporal_values = aci

    return main_value, temporal_values

# -----------------------------------------------------------------------------
def compute_ACI_without_transpose(spectro, wl, f, nb_windows, flim=None):
    """
    From https://github.com/patriceguyot/Acoustic_Indices and modified to match sspectro from seewave R package
    Compute the Acoustic Complexity Index from the spectrogram of an audio signal.

    Reference: Pieretti N, Farina A, Morri FD (2011) A new methodology to infer the singing activity of an avian community: the Acoustic Complexity Index (ACI). Ecological Indicators, 11, 868-873.

    Ported from the soundecology R package.

    spectro: the spectrogram of the audio signal
    j_bin: temporal size of the frame (in samples)
    flim: freq bounds of freq band to analyze in kHz


    """
    l = spectro.shape[0]
    times = np.array([(int(l / nb_windows * (j-1) + 1), int(l / nb_windows * j)) for j in range(1, nb_windows+1)]) - 1

    if flim is not None:
        flim = flim * 1000 * wl/f
        spectro = spectro[int(flim[0]): int(flim[1]), :]

    jspecs = [np.array(spectro[i[0]: i[1] + 1, :]) for i in times]  # sub-spectros of temporal size j

    aci = [np.sum(np.sum(abs(np.diff(jspec, axis=0)), axis=0)/np.sum(jspec, axis=0)) for jspec in
           jspecs]  # list of ACI values on each jspecs

    main_value = np.sum(aci)
    temporal_values = aci

    return main_value, temporal_values


if __name__ == '__main__':
    path_sound = "reduced.wav"
    # path_sound = "Example0_16_3587_1500.0_1.wav"
    sig, fs = sf.read(path_sound)
    # sig = sig[: fs*60 +1]
    # sig = sig[: fs*2 +1]
    wl = 512
    ovlp = 0
    hop = wl - (ovlp * wl / 100)
    nbWind = 5
    # np.abs(np.fft.fft(np.array([0.1, 1.2, 11, 48, 6, 9, 2, 48]), 8))
    # np.fft.fft(np.array([0.1, 1.2, 11, 48, 6, 9, 2, 48]), 8)
    # np.abs(np.fft.fft(np.array([0.1, 1.2, 11, 48, 6, 9, 2, 48]), 8))[1:8 // 2 + 1]
    spectrod, freqs = compute_spectrogram(sig, fs, windowLength=wl, windowHop=hop, square=False, windowType='hamming',
                                          centered=False, normalized=False)

    aci, temp_val = compute_ACI(spectrod, wl, fs, nb_windows= nbWind)
    # aci, temp_val = compute_ACI(spectrod, wl, fs, nb_windows=nbWind, flim=np.array([0.04, 0.75]))
    # aci1, temp_val = compute_ACI1(np.transpose(spectrod), wl, fs, 2)

    print(aci)
    # print(aci1)
    print('ok')
