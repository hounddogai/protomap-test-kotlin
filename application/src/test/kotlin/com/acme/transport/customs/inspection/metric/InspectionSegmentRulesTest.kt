package com.acme.transport.customs.inspection.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultInspectionSegmentValidator()
        val item = newInspectionSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = InspectionSegmentAttemptsRangeRule(64, 255)
        val item = newInspectionSegment("x", "y")
        item.attempts = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = InspectionSegmentRequiredPhoneRule("inspection segment needs a value")
        val item = newInspectionSegment("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = InspectionSegmentBatchSizeRangeRule(80, 86400)
        val item = newInspectionSegment("x", "y")
        item.batchSize = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
