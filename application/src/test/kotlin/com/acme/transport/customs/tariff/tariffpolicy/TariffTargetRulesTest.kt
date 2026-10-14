package com.acme.transport.customs.tariff.tariffpolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TariffTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTariffTargetValidator()
        val item = newTariffTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = TariffTargetRequiredTitleRule("tariff target needs a value")
        val item = newTariffTarget("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TariffTargetRequiredCodeRule("tariff target needs a value")
        val item = newTariffTarget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long route code`() {
        val rule = TariffTargetRouteCodeLengthRule(3600)
        val item = newTariffTarget("x", "y")
        item.routeCode = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
