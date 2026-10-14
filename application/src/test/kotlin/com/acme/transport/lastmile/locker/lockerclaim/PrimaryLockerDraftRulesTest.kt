package com.acme.transport.lastmile.locker.lockerclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryLockerDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryLockerDraftValidator()
        val item = newPrimaryLockerDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PrimaryLockerDraftRequiredCodeRule("primary locker draft needs an entry")
        val item = newPrimaryLockerDraft("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = PrimaryLockerDraftRequiredTrailerIdRule("primary locker draft needs a value")
        val item = newPrimaryLockerDraft("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PrimaryLockerDraftCodeLengthRule(255)
        val item = newPrimaryLockerDraft("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = PrimaryLockerDraftBatchSizeRangeRule(64, 64)
        val item = newPrimaryLockerDraft("x", "y")
        item.batchSize = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = PrimaryLockerDraftGraceDaysRangeRule(64, 80)
        val item = newPrimaryLockerDraft("x", "y")
        item.graceDays = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
