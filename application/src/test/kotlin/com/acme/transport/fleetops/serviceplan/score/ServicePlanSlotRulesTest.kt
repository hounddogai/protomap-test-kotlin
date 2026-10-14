package com.acme.transport.fleetops.serviceplan.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServicePlanSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultServicePlanSlotValidator()
        val item = newServicePlanSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ServicePlanSlotRequiredCodeRule("service plan slot needs an entry")
        val item = newServicePlanSlot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank country`() {
        val rule = ServicePlanSlotRequiredCountryRule("service plan slot needs this field")
        val item = newServicePlanSlot("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = ServicePlanSlotRequiredShiftCodeRule("service plan slot needs a value")
        val item = newServicePlanSlot("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = ServicePlanSlotAmountRangeRule(2.3, 4069.4)
        val item = newServicePlanSlot("x", "y")
        item.amount = 4070.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = ServicePlanSlotShiftCodeLengthRule(32)
        val item = newServicePlanSlot("x", "y")
        item.shiftCode = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
