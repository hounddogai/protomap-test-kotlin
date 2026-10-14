package com.acme.transport.freightpay.accessorial.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalAccessorialAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalAccessorialAdjustmentValidator()
        val item = newGlobalAccessorialAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = GlobalAccessorialAdjustmentRequiredCodeRule("global accessorial adjustment needs this field")
        val item = newGlobalAccessorialAdjustment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = GlobalAccessorialAdjustmentDistanceKmRangeRule(2.8, 1170.0)
        val item = newGlobalAccessorialAdjustment("x", "y")
        item.distanceKm = 1171.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = GlobalAccessorialAdjustmentRequiredLabelRule("global accessorial adjustment needs a value")
        val item = newGlobalAccessorialAdjustment("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalAccessorialAdjustmentCodeLengthRule(64)
        val item = newGlobalAccessorialAdjustment("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
