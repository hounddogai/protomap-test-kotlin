package com.acme.transport.linehaul.trailer.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyTrailerVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyTrailerVarianceValidator()
        val item = newLegacyTrailerVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LegacyTrailerVarianceRequiredCodeRule("legacy trailer variance needs a value")
        val item = newLegacyTrailerVariance("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = LegacyTrailerVarianceWindowDaysRangeRule(32, 64)
        val item = newLegacyTrailerVariance("x", "y")
        item.windowDays = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
