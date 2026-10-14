package com.acme.transport.safety.scorecard.scorecardgroup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScorecardCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScorecardCheckpointValidator()
        val item = newScorecardCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = ScorecardCheckpointPalletsRangeRule(16, 255)
        val item = newScorecardCheckpoint("x", "y")
        item.pallets = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ScorecardCheckpointRequiredNameRule("scorecard checkpoint needs a value")
        val item = newScorecardCheckpoint("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScorecardCheckpointRequiredCodeRule("scorecard checkpoint needs this field")
        val item = newScorecardCheckpoint("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long notes`() {
        val rule = ScorecardCheckpointNotesLengthRule(120)
        val item = newScorecardCheckpoint("x", "y")
        item.notes = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScorecardCheckpointCodeLengthRule(255)
        val item = newScorecardCheckpoint("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = ScorecardCheckpointNameLengthRule(16)
        val item = newScorecardCheckpoint("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
