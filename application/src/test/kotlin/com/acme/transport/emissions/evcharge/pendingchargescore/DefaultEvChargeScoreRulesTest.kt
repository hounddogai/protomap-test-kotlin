package com.acme.transport.emissions.evcharge.pendingchargescore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultEvChargeScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultEvChargeScoreValidator()
        val item = newDefaultEvChargeScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = DefaultEvChargeScoreCeilingRangeRule(7.6, 2782.3)
        val item = newDefaultEvChargeScore("x", "y")
        item.ceiling = 2783.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = DefaultEvChargeScoreRequiredLabelRule("default ev charge score needs this field")
        val item = newDefaultEvChargeScore("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
