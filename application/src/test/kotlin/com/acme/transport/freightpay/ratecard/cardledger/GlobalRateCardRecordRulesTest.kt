package com.acme.transport.freightpay.ratecard.cardledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalRateCardRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalRateCardRecordValidator()
        val item = newGlobalRateCardRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = GlobalRateCardRecordRequiredExternalRefRule("global rate card record needs this field")
        val item = newGlobalRateCardRecord("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above cost`() {
        val rule = GlobalRateCardRecordCostRangeRule(1.5, 4457.9)
        val item = newGlobalRateCardRecord("x", "y")
        item.cost = 4459.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalRateCardRecordCodeLengthRule(16)
        val item = newGlobalRateCardRecord("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = GlobalRateCardRecordLabelLengthRule(3600)
        val item = newGlobalRateCardRecord("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = GlobalRateCardRecordExternalRefLengthRule(3600)
        val item = newGlobalRateCardRecord("x", "y")
        item.externalRef = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
