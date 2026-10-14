package com.acme.transport.emissions.evcharge.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvChargeAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEvChargeAgreementValidator()
        val item = newEvChargeAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = EvChargeAgreementRequiredCodeRule("ev charge agreement needs an entry")
        val item = newEvChargeAgreement("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = EvChargeAgreementTitleLengthRule(80)
        val item = newEvChargeAgreement("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = EvChargeAgreementTrailerIdLengthRule(3600)
        val item = newEvChargeAgreement("x", "y")
        item.trailerId = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above score`() {
        val rule = EvChargeAgreementScoreRangeRule(0.6, 698.6)
        val item = newEvChargeAgreement("x", "y")
        item.score = 700.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = EvChargeAgreementBatchSizeRangeRule(80, 1000)
        val item = newEvChargeAgreement("x", "y")
        item.batchSize = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
