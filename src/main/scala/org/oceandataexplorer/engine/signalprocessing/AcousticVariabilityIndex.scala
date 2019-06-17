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
  * Class computing Acoustic Variability Index (AVI) over a 1-min zero-overlapped one-sided spectrum
  * Quotes mark refer to the original paper McPherson et al., 2016, Examining the value of the Acoustic Variability
  * Index in the characterisation of Australian marine soundscapes, Proceedings of ACOUSTICS
  */

case class AcousticVariabilityIndex() {

  /**
    * Computes AVI on a given spectrum from McPherson et al., 2016
    *
    * @param spectrum The one-sided ffts used values gathered in a 1-min zero-overlapped Fast Fourier Transform spectrogram
    * @return the AVI of the spectrogram
    */
  def compute(spectrum: Array[Array[Double]]): Array[Double] = {

    // Normalization factor (1/(Nf * Nt) in the original paper)
    val normalizationFactor = 1 / (spectrum.length * spectrum(1).length)

    // Transpose spectrogram to make computations easier
    val transposedSpectro = spectrum.transpose


    /*
      "normalising by the energy in both spectra"
     */

    val energyBothSpectra = transposedSpectro.map(_.grouped(2).
      map {
        case Array(a, b) => a + b
      }
      .toSeq)

    /*
     "subtracting the magnitude of adjacent zero-overlapped Fast Fourier Transform (FFT)
      spectra over a frequency band of interest"
     */

    val diffMagnitudeAdjacentFFT = transposedSpectro.zipWithIndex
      .map(x=>x._1.grouped(2).
        map {
          case Array(a, b) => b-a
        }
        .toSeq)

    // Compute AVI
    val AVI = diffMagnitudeAdjacentFFT
      .zipWithIndex
      .flatMap(x=>
        x._1.zip(energyBothSpectra(x._2)))
      .map { case (a,b)=> a/b}
      .sum

    AVI / normalizationFactor

  }

}
