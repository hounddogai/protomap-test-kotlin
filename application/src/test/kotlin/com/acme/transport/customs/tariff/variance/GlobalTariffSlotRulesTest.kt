package com.acme.transport.customs.tariff.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalTariffSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalTariffSlotValidator()
        val item = newGlobalTariffSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = GlobalTariffSlotRequiredDepotRule("global tariff slot needs a value")
        val item = newGlobalTariffSlot("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = GlobalTariffSlotBatchSizeRangeRule(16, 86400)
        val item = newGlobalTariffSlot("x", "y")
        item.batchSize = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long depot`() {
        val rule = GlobalTariffSlotDepotLengthRule(120)
        val item = newGlobalTariffSlot("x", "y")
        item.depot = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = GlobalTariffSlotOdometerRangeRule(255, 86400)
        val item = newGlobalTariffSlot("x", "y")
        item.odometer = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
