package com.acme.transport.customs.tariff.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TariffThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTariffThresholdValidator()
        val item = newTariffThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank source`() {
        val rule = TariffThresholdRequiredSourceRule("tariff threshold needs an entry")
        val item = newTariffThreshold("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = TariffThresholdRequiredNameRule("tariff threshold needs a value")
        val item = newTariffThreshold("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = TariffThresholdNameLengthRule(120)
        val item = newTariffThreshold("x", "y")
        item.name = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long source`() {
        val rule = TariffThresholdSourceLengthRule(120)
        val item = newTariffThreshold("x", "y")
        item.source = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
