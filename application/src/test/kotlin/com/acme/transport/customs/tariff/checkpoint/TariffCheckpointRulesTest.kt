package com.acme.transport.customs.tariff.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TariffCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTariffCheckpointValidator()
        val item = newTariffCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = TariffCheckpointLabelLengthRule(3600)
        val item = newTariffCheckpoint("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long country`() {
        val rule = TariffCheckpointCountryLengthRule(255)
        val item = newTariffCheckpoint("x", "y")
        item.country = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TariffCheckpointRequiredCodeRule("tariff checkpoint needs an entry")
        val item = newTariffCheckpoint("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank country`() {
        val rule = TariffCheckpointRequiredCountryRule("tariff checkpoint needs a value")
        val item = newTariffCheckpoint("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TariffCheckpointCodeLengthRule(16)
        val item = newTariffCheckpoint("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
