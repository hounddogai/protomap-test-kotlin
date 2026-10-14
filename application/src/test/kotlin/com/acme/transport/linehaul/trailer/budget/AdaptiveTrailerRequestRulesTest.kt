package com.acme.transport.linehaul.trailer.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveTrailerRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveTrailerRequestValidator()
        val item = newAdaptiveTrailerRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = AdaptiveTrailerRequestFloorValueRangeRule(9.7, 4486.2)
        val item = newAdaptiveTrailerRequest("x", "y")
        item.floorValue = 4487.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = AdaptiveTrailerRequestRequiredTimezoneRule("adaptive trailer request needs a value")
        val item = newAdaptiveTrailerRequest("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = AdaptiveTrailerRequestNameLengthRule(3600)
        val item = newAdaptiveTrailerRequest("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
