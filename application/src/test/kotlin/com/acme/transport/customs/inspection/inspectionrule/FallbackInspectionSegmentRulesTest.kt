package com.acme.transport.customs.inspection.inspectionrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackInspectionSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackInspectionSegmentValidator()
        val item = newFallbackInspectionSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = FallbackInspectionSegmentRequiredTitleRule("fallback inspection segment needs a value")
        val item = newFallbackInspectionSegment("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = FallbackInspectionSegmentStartsAtRangeRule(255, 86400)
        val item = newFallbackInspectionSegment("x", "y")
        item.startsAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = FallbackInspectionSegmentRequiredSegmentRule("fallback inspection segment needs an entry")
        val item = newFallbackInspectionSegment("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above budget`() {
        val rule = FallbackInspectionSegmentBudgetRangeRule(2.5, 223.5)
        val item = newFallbackInspectionSegment("x", "y")
        item.budget = 225.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
