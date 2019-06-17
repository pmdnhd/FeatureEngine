/** Copyright (C) 2017-2018 Project-ODE
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.oceandataexplorer.engine.signalprocessing

/**
 * Class that provides spectrogram to match sspectro R function from the seewave R package introduced by Sueur et al., 2008
 * in order to compute Acoustic Complexity Index from the same package
 *
 * @author Paul Nguyen HD, Alexandre Degurse
 *
 * @param nfft Size of fft-computation window
 * @param samplingRate The sampling rate of the signal
 * @param frequencyLimits Frequency band to analyze in kHz
 */
case class SpectrogramAcousticIndices
(
  nfft: Int,
  samplingRate: Float,
  frequencyLimits: Seq[Double] = Seq()
) extends FrequencyConvertible with Serializable {

  /**
   * Computes one-sided periodograms like in sspectro R function
   * An IllegalArgumentException is thrown if
   * fft.length != 2*spectrumSize (ie fft is not one-sided)
   *
   * @param fft The one-sided fft used values
   * @return the one-sided periodogram
   */
  def compute(fft: Array[Double]) : Array[Double] = {
    if (fft.length != 2*spectrumSize) {
      throw new IllegalArgumentException(s"Incorrect fft length (${fft.length}) for Periodogram" +
        s"it should be a one-sided (${2*spectrumSize}) FFT")
    }

    val oneSidedPeriodogram: Array[Double] = new Array[Double](spectrumSize)

    // Precompute first value and take the modulus
    oneSidedPeriodogram(0) = math.sqrt(fft(0)*fft(0) + fft(1)*fft(1))

    // Using while with local variables on purpose -- See performance test
    // scalastyle:off while var.local

    // Start at the second value and iterate
    var i: Int = 1
    var i2: Int = 2
    var i2p1: Int = 3
    val last = spectrumSize

    while (i < last) {
      oneSidedPeriodogram(i) = math.sqrt(fft(i2)*fft(i2) + fft(i2p1)*fft(i2p1))
      i += 1
      i2 = 2*i
      i2p1 = i2 + 1
    }

    // scalastyle:on while var.local

    // The first value is dropped according to the spectro R function
    // and only frequencies of interest are kept
    if (frequencyLimits.isEmpty) {
      oneSidedPeriodogram.drop(1)
    } else {
      val flimInkHz = frequencyLimits.map(x => x *1000 * nfft / samplingRate)
      oneSidedPeriodogram.slice(math.floor(flimInkHz.head).toInt,flimInkHz.last.toInt).drop(1)
    }
  }
}
