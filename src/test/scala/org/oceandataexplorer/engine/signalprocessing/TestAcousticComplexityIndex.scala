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

import org.scalatest.{FlatSpec, Matchers}

/**
 * Tests for Acoustic Complexity Index class
 *
 * @author Alexandre Degurse
 */
class TestAcousticComplexityIndex extends FlatSpec with Matchers {

  "ACI" should "compute" in {
    val signal = (0.0 to 2.0 by 1.0)
      .toArray
      .map(i => (0.0 until 256.0 by 1.0).toArray.map(_ + i*256.0))
    val fs = 1000.0f


    val fftClass = FFT(256, fs)
    val spectrum = signal.map(fftClass.compute)
    val aciClass = AcousticComplexityIndex(2)
    val acis = aciClass.compute(spectrum)

    acis.foreach(println)
    println(acis.sum)
  }
}
