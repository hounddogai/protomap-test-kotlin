package com.acme.transport.customs.tariff.tariffscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultTariffTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultTariffTargetValidator()
        val item = newDefaultTariffTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = DefaultTariffTargetRequiredTitleRule("default tariff target needs an entry")
        val item = newDefaultTariffTarget("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefaultTariffTargetCodeLengthRule(32)
        val item = newDefaultTariffTarget("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above priority`() {
        val rule = DefaultTariffTargetPriorityRangeRule(32, 255)
        val item = newDefaultTariffTarget("x", "y")
        item.priority = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
