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
 * @param nbWindows Number of temporal windows
 */
case class AcousticComplexityIndex(nbWindows: Int) {

  /**
   *
   * @param spectrum The one-sided ffts computed by FFT class
   * @return The ACI
   */
  def compute(spectrum: Array[Array[Double]]): Array[Double] = {
    // Extract the total number of columns of the spectrogram
    val ncol = spectrum.length

    if (ncol <= nbWindows) {
      throw new IllegalArgumentException(
        s"Incorrect spectrum length ($ncol) for ACI, must be lower than the" +
        s"number of windows ($nbWindows)"
      )
    }

    // Divide the number of columns into equal bins
    val times = (1 to nbWindows).map(
      j => ((ncol / nbWindows.toDouble * (j - 1)).toInt,
        (ncol / nbWindows.toDouble * j).toInt)
    )

    // Transpose spectrogram to make computations easier
    val transposedSpectrum = spectrum
      .map(spectrum => spectrum.grouped(2)
        .map(z => math.sqrt(z(0) * z(0) + z(1) * z(1)))
        .toArray
      )
      .transpose

    // Array that will be filled by the ACI values of a bin
    val arrayACI = Array.fill(nbWindows)(0.0)

    // scalastyle:off while var.local
    var j = 1

    while (j <= nbWindows) {
      // sub-spectrums of temporal size
      val subSpectrums: Array[Array[Double]] = transposedSpectrum.map(row => row.slice(times(j - 1)._1, times(j - 1)._2 + 1))
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
