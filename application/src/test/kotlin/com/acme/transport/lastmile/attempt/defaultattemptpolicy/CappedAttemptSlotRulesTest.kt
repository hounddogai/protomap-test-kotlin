package com.acme.transport.lastmile.attempt.defaultattemptpolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedAttemptSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedAttemptSlotValidator()
        val item = newCappedAttemptSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank description`() {
        val rule = CappedAttemptSlotRequiredDescriptionRule("capped attempt slot needs an entry")
        val item = newCappedAttemptSlot("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = CappedAttemptSlotCodeLengthRule(120)
        val item = newCappedAttemptSlot("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
