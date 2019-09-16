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

import org.oceandataexplorer.utils.test.OdeCustomMatchers
import org.scalatest.{FlatSpec, Matchers}

/**
 * Tests for Acoustic Complexity Index class
 *
 * @author Alexandre Degurse
 */
class TestAcousticComplexityIndex extends FlatSpec with Matchers with OdeCustomMatchers {

  /**
   * Maximum error allowed for [[OdeCustomMatchers.RmseMatcher]]
   */
  val maxRMSE = 1E-13

  val spectrumA = Array(
    Array(
      3.5,                0.0,                -0.5,
      1.2071067811865475,-0.5,                0.5,
      -0.5,                0.2071067811865476,-0.5,
      0.0
    ),Array(
      11.5,                0.0,               -0.5,
      1.2071067811865475,-0.5,                0.5,
      -0.5,                0.2071067811865476,-0.5,
      0.0
    ),Array(
      19.5,                0.0,               -0.5,
      1.2071067811865475,-0.5,                0.5,
      -0.5,                0.2071067811865476,-0.5,
      0.0
    ),Array(
      27.5,                0.0,               -0.5,
      1.2071067811865475,-0.5,                0.5,
      -0.5,                0.2071067811865476,-0.5,
      0.0
    ),Array(
      35.5,                0.0,               -0.5,
      1.2071067811865475,-0.5,                0.5,
      -0.5,                0.2071067811865476,-0.5,
      0.0
    ),Array(
      43.5,                0.0,               -0.5,
      1.2071067811865475,-0.5,                0.5,
      -0.5,                0.2071067811865476,-0.5,
      0.0
    ),Array(
      51.5,                0.0,               -0.5,
      1.2071067811865475,-0.5,                0.5,
      -0.5,                0.2071067811865476,-0.5,
      0.0
    ),Array(
      59.5,                0.0,               -0.5,
      1.2071067811865475,-0.5,                0.5,
      -0.5,                0.2071067811865476,-0.5,
      0.0
    )
  )

  val spectrumB = Array(Array(
    7.5, 0.0, -0.5, 2.513669746062924, -0.5, 1.2071067811865475, -0.5,
    0.7483028813327445, -0.5, 0.5, -0.5, 0.33408931895964933, -0.5,
    0.20710678118654757, -0.5, 0.09945618368982911, -0.5, 0.0), Array(23.5, 0.0,
    -0.5, 2.513669746062924, -0.5, 1.2071067811865475, -0.5, 0.7483028813327445,
    -0.5, 0.5, -0.5, 0.33408931895964933, -0.5, 0.20710678118654757, -0.5,
    0.09945618368982911, -0.5, 0.0), Array(39.5, 0.0, -0.5, 2.513669746062924,
    -0.5, 1.2071067811865475, -0.5, 0.7483028813327445, -0.5, 0.5, -0.5,
    0.33408931895964933, -0.5, 0.20710678118654757, -0.5, 0.09945618368982911,
    -0.5, 0.0), Array(55.5, 0.0, -0.5, 2.513669746062924, -0.5, 1.2071067811865475,
    -0.5, 0.7483028813327445, -0.5, 0.5, -0.5, 0.33408931895964933, -0.5,
    0.20710678118654757, -0.5, 0.09945618368982911, -0.5, 0.0), Array(71.5, 0.0,
    -0.5, 2.513669746062924, -0.5, 1.2071067811865475, -0.5, 0.7483028813327445,
    -0.5, 0.5, -0.5, 0.33408931895964933, -0.5, 0.20710678118654757, -0.5,
    0.09945618368982911, -0.5, 0.0), Array(87.5, 0.0, -0.5, 2.513669746062924,
    -0.5, 1.2071067811865475, -0.5, 0.7483028813327445, -0.5, 0.5, -0.5,
    0.33408931895964933, -0.5, 0.20710678118654757, -0.5, 0.09945618368982911,
    -0.5, 0.0), Array(103.5, 0.0, -0.5, 2.513669746062924, -0.5,
    1.2071067811865475, -0.5, 0.7483028813327445, -0.5, 0.5, -0.5,
    0.33408931895964933, -0.5, 0.20710678118654757, -0.5, 0.09945618368982911,
    -0.5, 0.0), Array(119.5, 0.0, -0.5, 2.513669746062924, -0.5,
    1.2071067811865475, -0.5, 0.7483028813327445, -0.5, 0.5, -0.5,
    0.33408931895964933, -0.5, 0.20710678118654757, -0.5, 0.09945618368982911,
    -0.5, 0.0), Array(135.5, 0.0, -0.5, 2.513669746062924, -0.5,
    1.2071067811865475, -0.5, 0.7483028813327445, -0.5, 0.5, -0.5,
    0.33408931895964933, -0.5, 0.20710678118654757, -0.5, 0.09945618368982911,
    -0.5, 0.0), Array(151.5, 0.0, -0.5, 2.513669746062924, -0.5,
    1.2071067811865475, -0.5, 0.7483028813327445, -0.5, 0.5, -0.5,
    0.33408931895964933, -0.5, 0.20710678118654757, -0.5, 0.09945618368982911,
    -0.5, 0.0), Array(167.5, 0.0, -0.5, 2.513669746062924, -0.5,
    1.2071067811865475, -0.5, 0.7483028813327445, -0.5, 0.5, -0.5,
    0.33408931895964933, -0.5, 0.20710678118654757, -0.5, 0.09945618368982911,
    -0.5, 0.0), Array(183.5, 0.0, -0.5, 2.513669746062924, -0.5,
    1.2071067811865475, -0.5, 0.7483028813327445, -0.5, 0.5, -0.5,
    0.33408931895964933, -0.5, 0.20710678118654757, -0.5, 0.09945618368982911,
    -0.5, 0.0), Array(199.5, 0.0, -0.5, 2.513669746062924, -0.5,
    1.2071067811865475, -0.5, 0.7483028813327445, -0.5, 0.5, -0.5,
    0.33408931895964933, -0.5, 0.20710678118654757, -0.5, 0.09945618368982911,
    -0.5, 0.0), Array(215.5, 0.0, -0.5, 2.513669746062924, -0.5,
    1.2071067811865475, -0.5, 0.7483028813327445, -0.5, 0.5, -0.5,
    0.33408931895964933, -0.5, 0.20710678118654757, -0.5, 0.09945618368982911,
    -0.5, 0.0), Array(231.5, 0.0, -0.5, 2.513669746062924, -0.5,
    1.2071067811865475, -0.5, 0.7483028813327445, -0.5, 0.5, -0.5,
    0.33408931895964933, -0.5, 0.20710678118654757, -0.5, 0.09945618368982911,
    -0.5, 0.0), Array(247.5, 0.0, -0.5, 2.513669746062924, -0.5,
    1.2071067811865475, -0.5, 0.7483028813327445, -0.5, 0.5, -0.5,
    0.33408931895964933, -0.5, 0.20710678118654757, -0.5, 0.09945618368982911,
    -0.5, 0.0))

  it should "compute ACI with 3 windows on spectrumA" in {
    val aciClass = AcousticComplexityIndex(3)
    val acis = aciClass.compute(spectrumA)
    val aciMainValue = acis.sum

    val expectedAcis = Array(
      0.5333333333333333, 0.19393939393939394, 0.10355987055016182
    )
    val expectedAciMainValue = 0.8308325978228891

    math.abs(expectedAciMainValue - aciMainValue) should be < maxRMSE
    acis should rmseMatch(expectedAcis)
  }

  it should "compute ACI with 5 windows on spectrumA" in {
    val aciClass = AcousticComplexityIndex(4)
    val acis = aciClass.compute(spectrumA)
    val aciMainValue = acis.sum

    val expectedAcis = Array(
      0.5333333333333333, 0.1702127659574468, 0.10126582278481013, 0.07207207207207207
    )
    val expectedAciMainValue = 0.8768839941476624

    math.abs(expectedAciMainValue - aciMainValue) should be < maxRMSE
    acis should rmseMatch(expectedAcis)
  }

  it should "compute ACI with 5 windows on spectrumB" in {
    val aciClass = AcousticComplexityIndex(5)
    val acis = aciClass.compute(spectrumB)
    val aciMainValue = acis.sum

    val expectedAcis = Array(
      0.45390070921985815, 0.14918414918414918, 0.08926080892608089,
      0.06368159203980099, 0.053691275167785234
    )
    val expectedAciMainValue = 0.8097185345376745

    math.abs(expectedAciMainValue - aciMainValue) should be < maxRMSE
    acis should rmseMatch(expectedAcis)
  }

  it should "compute ACI with 8 windows on spectrumB" in {
    val aciClass = AcousticComplexityIndex(8)
    val acis = aciClass.compute(spectrumB)
    val aciMainValue = acis.sum

    val expectedAcis = Array(
      0.5161290322580645, 0.16842105263157894, 0.10062893081761007,
      0.07174887892376682, 0.05574912891986063, 0.045584045584045586,
      0.03855421686746988, 0.033402922755741124
    )
    val expectedAciMainValue = 1.0302182087581375

    math.abs(expectedAciMainValue - aciMainValue) should be < maxRMSE
    acis should rmseMatch(expectedAcis)
  }

  it should "compute ACI with 3 windows on spectrumA with frequency bounds" in {
    val aciClass = AcousticComplexityIndex(3)
    val nfft = Some(8)
    val sampleRate = Some(10.0f)
    val lowFreqBound = Some(1.0)
    val highFreqBound = Some(8.0)
    val acis = aciClass.compute(spectrumA)
    val aciMainValue = acis.sum

    val expectedAcis = Array(
      0.5333333333333333, 0.19393939393939394, 0.10355987055016182
    )
    val expectedAciMainValue = 0.8308325978228891

    math.abs(expectedAciMainValue - aciMainValue) should be < maxRMSE
    acis should rmseMatch(expectedAcis)
  }
}
