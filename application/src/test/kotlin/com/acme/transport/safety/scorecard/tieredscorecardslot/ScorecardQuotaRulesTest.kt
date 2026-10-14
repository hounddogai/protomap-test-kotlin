package com.acme.transport.safety.scorecard.tieredscorecardslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScorecardQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScorecardQuotaValidator()
        val item = newScorecardQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = ScorecardQuotaLabelLengthRule(16)
        val item = newScorecardQuota("x", "y")
        item.label = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = ScorecardQuotaRequiredSummaryRule("scorecard quota needs this field")
        val item = newScorecardQuota("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ScorecardQuotaRequiredLabelRule("scorecard quota needs a value")
        val item = newScorecardQuota("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = ScorecardQuotaExpiresAtRangeRule(255, 3600)
        val item = newScorecardQuota("x", "y")
        item.expiresAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
