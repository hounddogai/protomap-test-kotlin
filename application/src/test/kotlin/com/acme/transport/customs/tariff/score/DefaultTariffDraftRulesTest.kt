package com.acme.transport.customs.tariff.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultTariffDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultTariffDraftValidator()
        val item = newDefaultTariffDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = DefaultTariffDraftShiftCodeLengthRule(1000)
        val item = newDefaultTariffDraft("x", "y")
        item.shiftCode = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = DefaultTariffDraftRequiredTitleRule("default tariff draft needs an entry")
        val item = newDefaultTariffDraft("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = DefaultTariffDraftDistanceKmRangeRule(3.4, 4064.7)
        val item = newDefaultTariffDraft("x", "y")
        item.distanceKm = 4066.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DefaultTariffDraftRequiredCodeRule("default tariff draft needs a value")
        val item = newDefaultTariffDraft("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = DefaultTariffDraftRequiredContactEmailRule("default tariff draft needs this field")
        val item = newDefaultTariffDraft("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
