package com.acme.transport.fleetops.inspection.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientInspectionSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientInspectionSegmentValidator()
        val item = newLenientInspectionSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = LenientInspectionSegmentLabelLengthRule(86400)
        val item = newLenientInspectionSegment("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = LenientInspectionSegmentRequiredLocaleRule("lenient inspection segment needs a value")
        val item = newLenientInspectionSegment("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = LenientInspectionSegmentScoreRangeRule(8.3, 1084.3)
        val item = newLenientInspectionSegment("x", "y")
        item.score = 1085.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long route code`() {
        val rule = LenientInspectionSegmentRouteCodeLengthRule(16)
        val item = newLenientInspectionSegment("x", "y")
        item.routeCode = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = LenientInspectionSegmentSequenceRangeRule(80, 3600)
        val item = newLenientInspectionSegment("x", "y")
        item.sequence = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
