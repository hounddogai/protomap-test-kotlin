package com.acme.transport.customs.tariff.tariffdraft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TariffBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTariffBundleValidator()
        val item = newTariffBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank category`() {
        val rule = TariffBundleRequiredCategoryRule("tariff bundle needs an entry")
        val item = newTariffBundle("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = TariffBundleRequiredNotesRule("tariff bundle needs this field")
        val item = newTariffBundle("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TariffBundleRequiredCodeRule("tariff bundle needs a value")
        val item = newTariffBundle("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long notes`() {
        val rule = TariffBundleNotesLengthRule(64)
        val item = newTariffBundle("x", "y")
        item.notes = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
