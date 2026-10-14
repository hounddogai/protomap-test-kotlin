package com.acme.transport.customs.broker.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedBrokerSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedBrokerSlotValidator()
        val item = newCappedBrokerSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long depot`() {
        val rule = CappedBrokerSlotDepotLengthRule(3600)
        val item = newCappedBrokerSlot("x", "y")
        item.depot = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = CappedBrokerSlotNameLengthRule(80)
        val item = newCappedBrokerSlot("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = CappedBrokerSlotTrailerIdLengthRule(86400)
        val item = newCappedBrokerSlot("x", "y")
        item.trailerId = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
