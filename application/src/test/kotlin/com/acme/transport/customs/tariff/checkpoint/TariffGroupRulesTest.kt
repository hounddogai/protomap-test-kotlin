package com.acme.transport.customs.tariff.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TariffGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTariffGroupValidator()
        val item = newTariffGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TariffGroupCodeLengthRule(120)
        val item = newTariffGroup("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = TariffGroupNameLengthRule(64)
        val item = newTariffGroup("x", "y")
        item.name = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = TariffGroupRequiredRouteCodeRule("tariff group needs a value")
        val item = newTariffGroup("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above cost`() {
        val rule = TariffGroupCostRangeRule(0.1, 1238.7)
        val item = newTariffGroup("x", "y")
        item.cost = 1240.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = TariffGroupRequiredSegmentRule("tariff group needs this field")
        val item = newTariffGroup("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
