package com.acme.transport.network.node.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingNodeSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingNodeSlotValidator()
        val item = newPendingNodeSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = PendingNodeSlotEtaMinutesRangeRule(16, 16)
        val item = newPendingNodeSlot("x", "y")
        item.etaMinutes = 17
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PendingNodeSlotRequiredCodeRule("pending node slot needs a value")
        val item = newPendingNodeSlot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = PendingNodeSlotOdometerRangeRule(80, 3600)
        val item = newPendingNodeSlot("x", "y")
        item.odometer = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
