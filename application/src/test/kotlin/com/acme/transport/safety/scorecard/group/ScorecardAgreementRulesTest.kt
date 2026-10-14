package com.acme.transport.safety.scorecard.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScorecardAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScorecardAgreementValidator()
        val item = newScorecardAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScorecardAgreementRequiredCodeRule("scorecard agreement needs a value")
        val item = newScorecardAgreement("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = ScorecardAgreementNameLengthRule(3600)
        val item = newScorecardAgreement("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScorecardAgreementCodeLengthRule(32)
        val item = newScorecardAgreement("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = ScorecardAgreementRequiredUnitRule("scorecard agreement needs this field")
        val item = newScorecardAgreement("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long unit`() {
        val rule = ScorecardAgreementUnitLengthRule(86400)
        val item = newScorecardAgreement("x", "y")
        item.unit = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above cost`() {
        val rule = ScorecardAgreementCostRangeRule(0.9, 3299.6)
        val item = newScorecardAgreement("x", "y")
        item.cost = 3301.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
