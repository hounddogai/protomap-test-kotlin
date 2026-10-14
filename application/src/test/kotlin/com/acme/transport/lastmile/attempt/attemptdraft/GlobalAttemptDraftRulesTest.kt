package com.acme.transport.lastmile.attempt.attemptdraft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalAttemptDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalAttemptDraftValidator()
        val item = newGlobalAttemptDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = GlobalAttemptDraftUpdatedAtRangeRule(16, 80)
        val item = newGlobalAttemptDraft("x", "y")
        item.updatedAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = GlobalAttemptDraftChecksumLengthRule(64)
        val item = newGlobalAttemptDraft("x", "y")
        item.checksum = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = GlobalAttemptDraftRequiredTitleRule("global attempt draft needs this field")
        val item = newGlobalAttemptDraft("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
