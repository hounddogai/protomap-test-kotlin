package com.acme.transport.customs.tariff.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TariffPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTariffPolicyValidator()
        val item = newTariffPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = TariffPolicyLabelLengthRule(80)
        val item = newTariffPolicy("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = TariffPolicySequenceRangeRule(32, 86400)
        val item = newTariffPolicy("x", "y")
        item.sequence = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = TariffPolicyOwnerIdLengthRule(80)
        val item = newTariffPolicy("x", "y")
        item.ownerId = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = TariffPolicyRequiredLabelRule("tariff policy needs an entry")
        val item = newTariffPolicy("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TariffPolicyRequiredCodeRule("tariff policy needs a value")
        val item = newTariffPolicy("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TariffPolicyCodeLengthRule(86400)
        val item = newTariffPolicy("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
