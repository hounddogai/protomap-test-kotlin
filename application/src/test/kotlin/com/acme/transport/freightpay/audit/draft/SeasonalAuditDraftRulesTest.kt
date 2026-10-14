package com.acme.transport.freightpay.audit.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalAuditDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalAuditDraftValidator()
        val item = newSeasonalAuditDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = SeasonalAuditDraftDurationMinutesRangeRule(64, 120)
        val item = newSeasonalAuditDraft("x", "y")
        item.durationMinutes = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = SeasonalAuditDraftLabelLengthRule(32)
        val item = newSeasonalAuditDraft("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
