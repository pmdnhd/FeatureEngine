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
 * Class computing Acoustic Complexity index over one-sided spectrum
 * of an audio signal

 * Reference: Pieretti N, Farina A, Morri FD (2011) A new methodology
 * to infer the singing activity of an avian community: the
 * Acoustic Complexity Index (ACI). Ecological Indicators, 11, 868-873
 *
 * Ported from the soundecology R package.
 *
 * @param nbWindows Number of temporal windows desired for ACI
 * @param samplingRate The sampling rate of the sound signal
 * @param lowFreqBound The lower bound of the band to analyse with ACI
 * @param highFreqBound The higher bound of the band to analyse with ACI
 */
case class AcousticComplexityIndex(
  nbWindows: Int,
  samplingRate: Option[Float] = None,
  nfft: Option[Int] = None,
  lowFreqBound: Option[Double] = None,
  highFreqBound: Option[Double] = None
) {

  // Index within the spectrum at which the analysis window starts
  lazy val analysisWindowStart = (lowFreqBound.get * nfft.get / samplingRate.get).toInt
  // Index within the spectrum at which the analysis window ends
  lazy val analysisWindowEnd = (highFreqBound.get * nfft.get / samplingRate.get).toInt

  /**
   *
   * @param spectrum The one-sided ffts computed by FFT class
   * @return The Acoustic Complexity Index computed over the spectrum
   */
  def compute(
    spectrum: Array[Array[Double]]
  ): Array[Double] = {
    // Extract the total number of columns of the spectrogram
    val spectrumTemporalSize = spectrum.length

    if (spectrumTemporalSize < (nbWindows * 2))  {
      throw new IllegalArgumentException(
        s"Incorrect spectrum length ($spectrumTemporalSize) for ACI, " +
        s"must be lower than the number of windows ($nbWindows)"
      )
    }

    val optionalParams = Array(samplingRate, nfft, lowFreqBound, highFreqBound)

    val (spectrumCut, windowStart, windowEnd) = if (
      optionalParams.map(_.isDefined).reduce((a,b) => a && b)
    ){
      (
        spectrum.map(fft => fft.slice(
          analysisWindowStart, analysisWindowEnd
        )),
        analysisWindowStart,
        analysisWindowEnd
      )
    } else if (optionalParams.map(_.isDefined).reduce((a,b) => a || b)) {
      throw new IllegalArgumentException(
        "Some parameters were not defined for the computation of ACI" +
        "on a specific frequency band."
      )
    } else {
      (spectrum, 0, nbWindows)
    }

    // Divide the number of columns into equal bins within
    // the analysis frequency range
    val times = (windowStart until windowEnd).map(
      j => ((spectrumTemporalSize / nbWindows.toDouble * j).toInt,
        (spectrumTemporalSize / nbWindows.toDouble * (j + 1)).toInt -1)
    )

    // Compute amplitude spectrum
    val transposedSpectrum = spectrumCut
      .map(fft => fft.grouped(2)
        .map(z => math.sqrt(z(0) * z(0) + z(1) * z(1)))
        .toArray
      )
      // Transpose spectrogram to make computations easier
      .transpose

    // Array that will be filled by the ACI values of a bin
    val arrayACI = Array.fill(nbWindows)(0.0)

    // scalastyle:off while var.local
    var j = 1

    while (j <= nbWindows) {
      // sub-spectrums of temporal size
      val subSpectrums: Array[Array[Double]] = transposedSpectrum
        .map(row => row.slice(times(j - 1)._1, times(j - 1)._2 + 1))

      // Sum over all the frequencies of the sub-spectrums (denominator of the ACI formula)
      val sums: Array[Double] = subSpectrums.map(_.sum)

      /* "Compute absolute difference between two adjacent values of intensity (Ik and I(k+1))
      in a single frequency bin" Pieretti et al., 2011 */

      val diff: Array[Array[Double]] = subSpectrums.zipWithIndex
        .map{ case (subSpectrum, idx) => {
            subSpectrum.sliding(2).map{
              case Array(a, b) => math.abs((a - b) / sums(idx))
            }.toArray
          }
        }


      // ACI for the sub-spectrum
      arrayACI(j - 1) = diff.map(_.sum).sum
      j = j + 1
    }

    // The total ACI is the sum of the computed ACIs
    arrayACI
  }
}
