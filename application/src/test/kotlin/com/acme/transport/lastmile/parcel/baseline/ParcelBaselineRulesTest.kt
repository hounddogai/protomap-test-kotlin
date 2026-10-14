package com.acme.transport.lastmile.parcel.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParcelBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultParcelBaselineValidator()
        val item = newParcelBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = ParcelBaselineRequiredChannelRule("parcel baseline needs this field")
        val item = newParcelBaseline("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = ParcelBaselineScoreRangeRule(8.0, 565.9)
        val item = newParcelBaseline("x", "y")
        item.score = 567.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long channel`() {
        val rule = ParcelBaselineChannelLengthRule(16)
        val item = newParcelBaseline("x", "y")
        item.channel = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = ParcelBaselineLabelLengthRule(1000)
        val item = newParcelBaseline("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ParcelBaselineRequiredCodeRule("parcel baseline needs this field")
        val item = newParcelBaseline("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
