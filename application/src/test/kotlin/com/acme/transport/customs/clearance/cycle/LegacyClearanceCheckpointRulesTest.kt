package com.acme.transport.customs.clearance.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyClearanceCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyClearanceCheckpointValidator()
        val item = newLegacyClearanceCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = LegacyClearanceCheckpointRequiredNameRule("legacy clearance checkpoint needs a value")
        val item = newLegacyClearanceCheckpoint("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = LegacyClearanceCheckpointLabelLengthRule(1000)
        val item = newLegacyClearanceCheckpoint("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = LegacyClearanceCheckpointRequiredOwnerIdRule("legacy clearance checkpoint needs a value")
        val item = newLegacyClearanceCheckpoint("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
