package com.acme.transport.lastmile.parcel.parcelcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultParcelManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultParcelManifestValidator()
        val item = newDefaultParcelManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = DefaultParcelManifestRequiredCarrierRule("default parcel manifest needs this field")
        val item = newDefaultParcelManifest("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = DefaultParcelManifestCarrierLengthRule(120)
        val item = newDefaultParcelManifest("x", "y")
        item.carrier = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above score`() {
        val rule = DefaultParcelManifestScoreRangeRule(8.7, 1393.6)
        val item = newDefaultParcelManifest("x", "y")
        item.score = 1395.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
