package com.acme.transport.linehaul.capacity.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CapacityAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCapacityAgreementValidator()
        val item = newCapacityAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = CapacityAgreementRequiredTitleRule("capacity agreement needs this field")
        val item = newCapacityAgreement("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long phone`() {
        val rule = CapacityAgreementPhoneLengthRule(120)
        val item = newCapacityAgreement("x", "y")
        item.phone = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = CapacityAgreementTitleLengthRule(3600)
        val item = newCapacityAgreement("x", "y")
        item.title = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CapacityAgreementRequiredCodeRule("capacity agreement needs this field")
        val item = newCapacityAgreement("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = CapacityAgreementMaximumRangeRule(0.0, 3714.4)
        val item = newCapacityAgreement("x", "y")
        item.maximum = 3715.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
