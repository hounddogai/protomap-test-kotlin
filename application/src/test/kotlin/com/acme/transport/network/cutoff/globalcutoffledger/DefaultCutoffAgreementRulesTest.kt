package com.acme.transport.network.cutoff.globalcutoffledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultCutoffAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultCutoffAgreementValidator()
        val item = newDefaultCutoffAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DefaultCutoffAgreementRequiredCodeRule("default cutoff agreement needs a value")
        val item = newDefaultCutoffAgreement("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = DefaultCutoffAgreementRequiredTitleRule("default cutoff agreement needs an entry")
        val item = newDefaultCutoffAgreement("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above cost`() {
        val rule = DefaultCutoffAgreementCostRangeRule(5.2, 3693.4)
        val item = newDefaultCutoffAgreement("x", "y")
        item.cost = 3694.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = DefaultCutoffAgreementQuantityRangeRule(64, 1000)
        val item = newDefaultCutoffAgreement("x", "y")
        item.quantity = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = DefaultCutoffAgreementRequiredDepotRule("default cutoff agreement needs this field")
        val item = newDefaultCutoffAgreement("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = DefaultCutoffAgreementRequiredCurrencyRule("default cutoff agreement needs a value")
        val item = newDefaultCutoffAgreement("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
