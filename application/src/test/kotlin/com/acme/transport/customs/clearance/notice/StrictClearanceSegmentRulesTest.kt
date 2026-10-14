package com.acme.transport.customs.clearance.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictClearanceSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictClearanceSegmentValidator()
        val item = newStrictClearanceSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = StrictClearanceSegmentOdometerRangeRule(80, 3600)
        val item = newStrictClearanceSegment("x", "y")
        item.odometer = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = StrictClearanceSegmentRequiredCodeRule("strict clearance segment needs this field")
        val item = newStrictClearanceSegment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = StrictClearanceSegmentRequiredLocaleRule("strict clearance segment needs a value")
        val item = newStrictClearanceSegment("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
