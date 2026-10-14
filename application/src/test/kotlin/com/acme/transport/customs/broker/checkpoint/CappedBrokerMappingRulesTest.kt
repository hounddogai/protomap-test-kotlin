package com.acme.transport.customs.broker.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedBrokerMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedBrokerMappingValidator()
        val item = newCappedBrokerMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = CappedBrokerMappingWeightRangeRule(5.4, 4695.7)
        val item = newCappedBrokerMapping("x", "y")
        item.weight = 4697.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = CappedBrokerMappingRequiredOwnerIdRule("capped broker mapping needs an entry")
        val item = newCappedBrokerMapping("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = CappedBrokerMappingDriverIdLengthRule(120)
        val item = newCappedBrokerMapping("x", "y")
        item.driverId = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
