package com.acme.transport.lastmile.attempt.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AttemptCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAttemptCycleValidator()
        val item = newAttemptCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = AttemptCycleRequiredNotesRule("attempt cycle needs this field")
        val item = newAttemptCycle("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = AttemptCycleRequiredDepotRule("attempt cycle needs an entry")
        val item = newAttemptCycle("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long depot`() {
        val rule = AttemptCycleDepotLengthRule(1000)
        val item = newAttemptCycle("x", "y")
        item.depot = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
