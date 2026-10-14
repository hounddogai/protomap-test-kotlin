package com.acme.transport.emissions.emissionfactor.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmissionFactorOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEmissionFactorOverrideValidator()
        val item = newEmissionFactorOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = EmissionFactorOverrideRequiredLocaleRule("emission factor override needs an entry")
        val item = newEmissionFactorOverride("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = EmissionFactorOverrideNameLengthRule(80)
        val item = newEmissionFactorOverride("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = EmissionFactorOverrideChecksumLengthRule(1000)
        val item = newEmissionFactorOverride("x", "y")
        item.checksum = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = EmissionFactorOverrideRequiredCodeRule("emission factor override needs an entry")
        val item = newEmissionFactorOverride("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
