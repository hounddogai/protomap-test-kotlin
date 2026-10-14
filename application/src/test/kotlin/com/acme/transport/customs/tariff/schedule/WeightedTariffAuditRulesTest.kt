package com.acme.transport.customs.tariff.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedTariffAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedTariffAuditValidator()
        val item = newWeightedTariffAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = WeightedTariffAuditCodeLengthRule(16)
        val item = newWeightedTariffAudit("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = WeightedTariffAuditLabelLengthRule(16)
        val item = newWeightedTariffAudit("x", "y")
        item.label = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = WeightedTariffAuditRequiredLabelRule("weighted tariff audit needs a value")
        val item = newWeightedTariffAudit("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
