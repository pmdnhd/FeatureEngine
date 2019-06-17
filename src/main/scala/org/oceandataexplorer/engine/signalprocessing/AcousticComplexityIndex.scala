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
 * Class computing .... over one-sided spectrum
 * @param nbWindows Number of windows
 */
case class AcousticComplexityIndex(nbWindows: Int) {

  /**
   * Computes one-sided periodograms like in Matlab
   * and Python using the periodogram method (mode 'psd')
   * An IllegalArgumentException is thrown if
   * fft.length != 2*spectrumSize (ie fft is not one-sided)
   *
   * @param spectrum The one-sided ffts used values gathered in a spectromgram
   * @return the one-sided periodogram
   */
  def compute(spectrum: Array[Array[Double]]): Array[Double] = {
    // Extract the total nber of columns of the spectrumgram
    val ncol = spectrum.length

    // Divide the number of columns into equal bins
    val times = (1 to nbWindows).map(j => ((ncol / nbWindows.toDouble * (j - 1)).toInt,
      (ncol / nbWindows.toDouble * j).toInt))

    // Transpose spectrogram to make computations easier
    val transposedSpectro = spectrum.transpose

    // Array that will be filled by the ACI values of a bin
    val arrayACI = Array.fill(nbWindows)(0.0)

    // scalastyle:off while var.local
    var j = 1

    while (j <= nbWindows) {
      // sub-spectrums of temporal size
      val subSpectros = transposedSpectro.map(row => row.slice(times(j - 1)._1, times(j - 1)._2))
      // Sum over all the frequencies of the sub-spectrums (denominator of the ACI formula)
      val sums = subSpectros.map(_.sum)

      /* "Compute absolute difference between two adjacent values of intensity (Ik and I(k+1))
      in a single frequency bin" Pieretti et al., 2011 */

      val diff = subSpectros.zipWithIndex
        .map(x => x._1.sliding(2).map{
          case Array(a, b) =>
            scala.math.abs((a - b) / sums(x._2))
        }
      )

      // ACI for the sub-spectrum
      arrayACI(j - 1) = diff.map(_.sum).sum
      j = j + 1
    }

    arrayACI
    // The total ACI is the sum of the computed ACIs
  }
}
