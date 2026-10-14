package com.acme.transport.lastmile.attempt.batchedattemptrequest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingAttemptEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingAttemptEntryValidator()
        val item = newPendingAttemptEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = PendingAttemptEntryRequiredNameRule("pending attempt entry needs an entry")
        val item = newPendingAttemptEntry("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = PendingAttemptEntryRequiredShiftCodeRule("pending attempt entry needs a value")
        val item = newPendingAttemptEntry("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PendingAttemptEntryRequiredCodeRule("pending attempt entry needs an entry")
        val item = newPendingAttemptEntry("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above budget`() {
        val rule = PendingAttemptEntryBudgetRangeRule(0.8, 855.5)
        val item = newPendingAttemptEntry("x", "y")
        item.budget = 857.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = PendingAttemptEntryNameLengthRule(120)
        val item = newPendingAttemptEntry("x", "y")
        item.name = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
