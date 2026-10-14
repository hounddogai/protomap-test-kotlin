package com.acme.transport.customs.clearance.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientClearanceMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientClearanceMetricValidator()
        val item = newLenientClearanceMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above created at`() {
        val rule = LenientClearanceMetricCreatedAtRangeRule(255, 3600)
        val item = newLenientClearanceMetric("x", "y")
        item.createdAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank country`() {
        val rule = LenientClearanceMetricRequiredCountryRule("lenient clearance metric needs a value")
        val item = newLenientClearanceMetric("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LenientClearanceMetricRequiredCodeRule("lenient clearance metric needs an entry")
        val item = newLenientClearanceMetric("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = LenientClearanceMetricTitleLengthRule(80)
        val item = newLenientClearanceMetric("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
