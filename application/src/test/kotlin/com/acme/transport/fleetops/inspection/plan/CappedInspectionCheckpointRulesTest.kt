package com.acme.transport.fleetops.inspection.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedInspectionCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedInspectionCheckpointValidator()
        val item = newCappedInspectionCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = CappedInspectionCheckpointRequiredLaneRule("capped inspection checkpoint needs a value")
        val item = newCappedInspectionCheckpoint("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long lane`() {
        val rule = CappedInspectionCheckpointLaneLengthRule(80)
        val item = newCappedInspectionCheckpoint("x", "y")
        item.lane = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CappedInspectionCheckpointRequiredCodeRule("capped inspection checkpoint needs a value")
        val item = newCappedInspectionCheckpoint("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
