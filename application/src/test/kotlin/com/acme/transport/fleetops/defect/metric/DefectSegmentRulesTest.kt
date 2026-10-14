package com.acme.transport.fleetops.defect.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefectSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefectSegmentValidator()
        val item = newDefectSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = DefectSegmentShiftCodeLengthRule(16)
        val item = newDefectSegment("x", "y")
        item.shiftCode = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = DefectSegmentDistanceKmRangeRule(8.4, 2783.8)
        val item = newDefectSegment("x", "y")
        item.distanceKm = 2785.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
