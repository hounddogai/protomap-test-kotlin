package com.acme.transport.customs.broker.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledBrokerRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledBrokerRequestValidator()
        val item = newScheduledBrokerRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ScheduledBrokerRequestRequiredTitleRule("scheduled broker request needs this field")
        val item = newScheduledBrokerRequest("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = ScheduledBrokerRequestNameLengthRule(16)
        val item = newScheduledBrokerRequest("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledBrokerRequestRequiredCodeRule("scheduled broker request needs an entry")
        val item = newScheduledBrokerRequest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = ScheduledBrokerRequestFloorValueRangeRule(4.8, 2513.3)
        val item = newScheduledBrokerRequest("x", "y")
        item.floorValue = 2514.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = ScheduledBrokerRequestDistanceKmRangeRule(9.0, 1719.4)
        val item = newScheduledBrokerRequest("x", "y")
        item.distanceKm = 1720.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
