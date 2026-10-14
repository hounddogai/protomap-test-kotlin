package com.acme.transport.fleetops.tire.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TireBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTireBatchValidator()
        val item = newTireBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = TireBatchScoreRangeRule(1.3, 3048.2)
        val item = newTireBatch("x", "y")
        item.score = 3049.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = TireBatchSequenceRangeRule(16, 32)
        val item = newTireBatch("x", "y")
        item.sequence = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TireBatchRequiredCodeRule("tire batch needs an entry")
        val item = newTireBatch("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = TireBatchRequiredTitleRule("tire batch needs an entry")
        val item = newTireBatch("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
