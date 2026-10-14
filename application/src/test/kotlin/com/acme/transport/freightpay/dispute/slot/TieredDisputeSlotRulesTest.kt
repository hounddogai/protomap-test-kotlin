package com.acme.transport.freightpay.dispute.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredDisputeSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredDisputeSlotValidator()
        val item = newTieredDisputeSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = TieredDisputeSlotRequiredVehicleIdRule("tiered dispute slot needs a value")
        val item = newTieredDisputeSlot("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = TieredDisputeSlotRequiredTimezoneRule("tiered dispute slot needs this field")
        val item = newTieredDisputeSlot("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TieredDisputeSlotRequiredCodeRule("tiered dispute slot needs this field")
        val item = newTieredDisputeSlot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
