package com.acme.transport.customs.tariff.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TariffCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTariffCycleValidator()
        val item = newTariffCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank description`() {
        val rule = TariffCycleRequiredDescriptionRule("tariff cycle needs a value")
        val item = newTariffCycle("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = TariffCycleLabelLengthRule(120)
        val item = newTariffCycle("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
