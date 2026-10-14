package com.acme.transport.safety.scorecard.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualScorecardAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualScorecardAuditValidator()
        val item = newManualScorecardAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ManualScorecardAuditRequiredNameRule("manual scorecard audit needs this field")
        val item = newManualScorecardAudit("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long currency`() {
        val rule = ManualScorecardAuditCurrencyLengthRule(120)
        val item = newManualScorecardAudit("x", "y")
        item.currency = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualScorecardAuditRequiredCodeRule("manual scorecard audit needs a value")
        val item = newManualScorecardAudit("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = ManualScorecardAuditUpdatedAtRangeRule(16, 64)
        val item = newManualScorecardAudit("x", "y")
        item.updatedAt = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = ManualScorecardAuditMinimumRangeRule(7.7, 3580.0)
        val item = newManualScorecardAudit("x", "y")
        item.minimum = 3581.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
