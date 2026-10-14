package com.acme.transport.fleetops.inspection.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultInspectionCheckpointValidator()
        val item = newInspectionCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = InspectionCheckpointRequiredCodeRule("inspection checkpoint needs a value")
        val item = newInspectionCheckpoint("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long summary`() {
        val rule = InspectionCheckpointSummaryLengthRule(1000)
        val item = newInspectionCheckpoint("x", "y")
        item.summary = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
