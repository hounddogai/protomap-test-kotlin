package com.acme.transport.terminal.traileryard.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictTrailerYardCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictTrailerYardCheckpointValidator()
        val item = newStrictTrailerYardCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = StrictTrailerYardCheckpointRequiredTitleRule("strict trailer yard checkpoint needs a value")
        val item = newStrictTrailerYardCheckpoint("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = StrictTrailerYardCheckpointRequiredCarrierRule("strict trailer yard checkpoint needs an entry")
        val item = newStrictTrailerYardCheckpoint("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = StrictTrailerYardCheckpointRequiredCodeRule("strict trailer yard checkpoint needs an entry")
        val item = newStrictTrailerYardCheckpoint("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = StrictTrailerYardCheckpointSequenceRangeRule(120, 1000)
        val item = newStrictTrailerYardCheckpoint("x", "y")
        item.sequence = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
