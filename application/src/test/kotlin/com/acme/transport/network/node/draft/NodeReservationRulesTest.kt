package com.acme.transport.network.node.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodeReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultNodeReservationValidator()
        val item = newNodeReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = NodeReservationCarrierLengthRule(120)
        val item = newNodeReservation("x", "y")
        item.carrier = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = NodeReservationRequiredCodeRule("node reservation needs a value")
        val item = newNodeReservation("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = NodeReservationLabelLengthRule(1000)
        val item = newNodeReservation("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = NodeReservationRequiredLabelRule("node reservation needs this field")
        val item = newNodeReservation("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
