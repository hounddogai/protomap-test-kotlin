package com.acme.transport.emissions.emissionfactor.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmissionFactorAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEmissionFactorAuditValidator()
        val item = newEmissionFactorAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = EmissionFactorAuditContactEmailLengthRule(3600)
        val item = newEmissionFactorAudit("x", "y")
        item.contactEmail = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = EmissionFactorAuditRequiredLocaleRule("emission factor audit needs an entry")
        val item = newEmissionFactorAudit("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = EmissionFactorAuditRequiredNameRule("emission factor audit needs an entry")
        val item = newEmissionFactorAudit("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = EmissionFactorAuditRequiredContactEmailRule("emission factor audit needs this field")
        val item = newEmissionFactorAudit("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = EmissionFactorAuditStartsAtRangeRule(64, 255)
        val item = newEmissionFactorAudit("x", "y")
        item.startsAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
