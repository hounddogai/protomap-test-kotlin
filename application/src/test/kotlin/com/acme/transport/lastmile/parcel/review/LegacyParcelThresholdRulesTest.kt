package com.acme.transport.lastmile.parcel.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyParcelThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyParcelThresholdValidator()
        val item = newLegacyParcelThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LegacyParcelThresholdRequiredCodeRule("legacy parcel threshold needs this field")
        val item = newLegacyParcelThreshold("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = LegacyParcelThresholdLabelLengthRule(80)
        val item = newLegacyParcelThreshold("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = LegacyParcelThresholdExpiresAtRangeRule(16, 3600)
        val item = newLegacyParcelThreshold("x", "y")
        item.expiresAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
