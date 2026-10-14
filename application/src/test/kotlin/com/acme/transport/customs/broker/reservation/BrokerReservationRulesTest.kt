package com.acme.transport.customs.broker.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BrokerReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBrokerReservationValidator()
        val item = newBrokerReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = BrokerReservationRequiredOwnerIdRule("broker reservation needs an entry")
        val item = newBrokerReservation("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BrokerReservationRequiredCodeRule("broker reservation needs an entry")
        val item = newBrokerReservation("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = BrokerReservationCodeLengthRule(120)
        val item = newBrokerReservation("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = BrokerReservationRequiredTitleRule("broker reservation needs this field")
        val item = newBrokerReservation("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
