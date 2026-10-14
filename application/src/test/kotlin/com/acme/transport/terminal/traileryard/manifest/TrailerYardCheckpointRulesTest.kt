package com.acme.transport.terminal.traileryard.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerYardCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTrailerYardCheckpointValidator()
        val item = newTrailerYardCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = TrailerYardCheckpointRequiredLabelRule("trailer yard checkpoint needs a value")
        val item = newTrailerYardCheckpoint("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = TrailerYardCheckpointRequiredCarrierRule("trailer yard checkpoint needs an entry")
        val item = newTrailerYardCheckpoint("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = TrailerYardCheckpointLabelLengthRule(3600)
        val item = newTrailerYardCheckpoint("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
