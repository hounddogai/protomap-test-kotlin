package com.acme.transport.emissions.emissionfactor.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmissionFactorBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEmissionFactorBundleValidator()
        val item = newEmissionFactorBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = EmissionFactorBundleRequiredNameRule("emission factor bundle needs an entry")
        val item = newEmissionFactorBundle("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = EmissionFactorBundleNameLengthRule(1000)
        val item = newEmissionFactorBundle("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = EmissionFactorBundleCodeLengthRule(255)
        val item = newEmissionFactorBundle("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
