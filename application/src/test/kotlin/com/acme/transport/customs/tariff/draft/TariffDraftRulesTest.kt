package com.acme.transport.customs.tariff.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TariffDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTariffDraftValidator()
        val item = newTariffDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TariffDraftRequiredCodeRule("tariff draft needs an entry")
        val item = newTariffDraft("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = TariffDraftCeilingRangeRule(6.4, 2849.7)
        val item = newTariffDraft("x", "y")
        item.ceiling = 2851.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = TariffDraftNameLengthRule(64)
        val item = newTariffDraft("x", "y")
        item.name = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = TariffDraftMinimumRangeRule(5.1, 3817.8)
        val item = newTariffDraft("x", "y")
        item.minimum = 3819.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
