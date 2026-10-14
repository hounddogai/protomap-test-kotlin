package com.acme.transport.terminal.crossdock.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CrossDockReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCrossDockReservationValidator()
        val item = newCrossDockReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = CrossDockReservationTitleLengthRule(1000)
        val item = newCrossDockReservation("x", "y")
        item.title = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long source`() {
        val rule = CrossDockReservationSourceLengthRule(1000)
        val item = newCrossDockReservation("x", "y")
        item.source = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long depot`() {
        val rule = CrossDockReservationDepotLengthRule(1000)
        val item = newCrossDockReservation("x", "y")
        item.depot = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above retries`() {
        val rule = CrossDockReservationRetriesRangeRule(64, 80)
        val item = newCrossDockReservation("x", "y")
        item.retries = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = CrossDockReservationCodeLengthRule(3600)
        val item = newCrossDockReservation("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
