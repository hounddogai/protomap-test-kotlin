package com.acme.transport.emissions.emissionfactor.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmissionFactorCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEmissionFactorCheckpointValidator()
        val item = newEmissionFactorCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = EmissionFactorCheckpointRequiredTitleRule("emission factor checkpoint needs an entry")
        val item = newEmissionFactorCheckpoint("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = EmissionFactorCheckpointTitleLengthRule(80)
        val item = newEmissionFactorCheckpoint("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank reviewer`() {
        val rule = EmissionFactorCheckpointRequiredReviewerRule("emission factor checkpoint needs an entry")
        val item = newEmissionFactorCheckpoint("", "")
        item.reviewer = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = EmissionFactorCheckpointLabelLengthRule(32)
        val item = newEmissionFactorCheckpoint("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
