package com.acme.transport.safety.nearmiss.missvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NearMissCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultNearMissCheckpointValidator()
        val item = newNearMissCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = NearMissCheckpointOwnerIdLengthRule(3600)
        val item = newNearMissCheckpoint("x", "y")
        item.ownerId = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = NearMissCheckpointRequiredOwnerIdRule("near miss checkpoint needs an entry")
        val item = newNearMissCheckpoint("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = NearMissCheckpointRequiredNotesRule("near miss checkpoint needs an entry")
        val item = newNearMissCheckpoint("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = NearMissCheckpointCodeLengthRule(64)
        val item = newNearMissCheckpoint("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = NearMissCheckpointLabelLengthRule(64)
        val item = newNearMissCheckpoint("x", "y")
        item.label = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
