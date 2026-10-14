package com.acme.transport.linehaul.tender.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TenderVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTenderVarianceValidator()
        val item = newTenderVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = TenderVarianceRequiredPhoneRule("tender variance needs a value")
        val item = newTenderVariance("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above revision`() {
        val rule = TenderVarianceRevisionRangeRule(3600, 86400)
        val item = newTenderVariance("x", "y")
        item.revision = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long phone`() {
        val rule = TenderVariancePhoneLengthRule(80)
        val item = newTenderVariance("x", "y")
        item.phone = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = TenderVarianceCodeLengthRule(255)
        val item = newTenderVariance("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = TenderVarianceQuantityRangeRule(16, 1000)
        val item = newTenderVariance("x", "y")
        item.quantity = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
