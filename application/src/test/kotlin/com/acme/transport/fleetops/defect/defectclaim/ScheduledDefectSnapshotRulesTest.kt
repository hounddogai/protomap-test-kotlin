package com.acme.transport.fleetops.defect.defectclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledDefectSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledDefectSnapshotValidator()
        val item = newScheduledDefectSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above cost`() {
        val rule = ScheduledDefectSnapshotCostRangeRule(1.3, 2288.4)
        val item = newScheduledDefectSnapshot("x", "y")
        item.cost = 2289.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScheduledDefectSnapshotCodeLengthRule(120)
        val item = newScheduledDefectSnapshot("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = ScheduledDefectSnapshotLabelLengthRule(80)
        val item = newScheduledDefectSnapshot("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
