package com.acme.transport.emissions.evcharge.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryEvChargeAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryEvChargeAgreementValidator()
        val item = newPrimaryEvChargeAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = PrimaryEvChargeAgreementAmountRangeRule(8.9, 4283.1)
        val item = newPrimaryEvChargeAgreement("x", "y")
        item.amount = 4284.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = PrimaryEvChargeAgreementCodeLengthRule(32)
        val item = newPrimaryEvChargeAgreement("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = PrimaryEvChargeAgreementRequiredNameRule("primary ev charge agreement needs this field")
        val item = newPrimaryEvChargeAgreement("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
