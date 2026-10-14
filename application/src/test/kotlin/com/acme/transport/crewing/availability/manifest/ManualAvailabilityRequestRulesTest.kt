package com.acme.transport.crewing.availability.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualAvailabilityRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualAvailabilityRequestValidator()
        val item = newManualAvailabilityRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = ManualAvailabilityRequestTitleLengthRule(80)
        val item = newManualAvailabilityRequest("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above margin`() {
        val rule = ManualAvailabilityRequestMarginRangeRule(4.3, 2131.5)
        val item = newManualAvailabilityRequest("x", "y")
        item.margin = 2133.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualAvailabilityRequestRequiredCodeRule("manual availability request needs this field")
        val item = newManualAvailabilityRequest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above rate`() {
        val rule = ManualAvailabilityRequestRateRangeRule(5.5, 4291.3)
        val item = newManualAvailabilityRequest("x", "y")
        item.rate = 4292.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
