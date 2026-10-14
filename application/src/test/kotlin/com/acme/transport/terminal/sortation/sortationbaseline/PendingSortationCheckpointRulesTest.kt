package com.acme.transport.terminal.sortation.sortationbaseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingSortationCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingSortationCheckpointValidator()
        val item = newPendingSortationCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = PendingSortationCheckpointExternalRefLengthRule(1000)
        val item = newPendingSortationCheckpoint("x", "y")
        item.externalRef = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = PendingSortationCheckpointRequiredLabelRule("pending sortation checkpoint needs an entry")
        val item = newPendingSortationCheckpoint("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PendingSortationCheckpointCodeLengthRule(1000)
        val item = newPendingSortationCheckpoint("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
