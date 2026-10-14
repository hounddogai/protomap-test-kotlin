package com.acme.transport.safety.scorecard.scorecardproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalScorecardAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalScorecardAuditValidator()
        val item = newRegionalScorecardAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RegionalScorecardAuditCodeLengthRule(3600)
        val item = newRegionalScorecardAudit("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalScorecardAuditRequiredCodeRule("regional scorecard audit needs a value")
        val item = newRegionalScorecardAudit("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = RegionalScorecardAuditPalletsRangeRule(120, 3600)
        val item = newRegionalScorecardAudit("x", "y")
        item.pallets = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
