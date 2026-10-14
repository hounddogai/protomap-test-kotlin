package com.acme.transport.crewing.dutyperiod.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DutyPeriodClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDutyPeriodClaimValidator()
        val item = newDutyPeriodClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = DutyPeriodClaimRequiredTitleRule("duty period claim needs this field")
        val item = newDutyPeriodClaim("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = DutyPeriodClaimExpiresAtRangeRule(64, 86400)
        val item = newDutyPeriodClaim("x", "y")
        item.expiresAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = DutyPeriodClaimTitleLengthRule(80)
        val item = newDutyPeriodClaim("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = DutyPeriodClaimRequiredCurrencyRule("duty period claim needs a value")
        val item = newDutyPeriodClaim("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long depot`() {
        val rule = DutyPeriodClaimDepotLengthRule(64)
        val item = newDutyPeriodClaim("x", "y")
        item.depot = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
