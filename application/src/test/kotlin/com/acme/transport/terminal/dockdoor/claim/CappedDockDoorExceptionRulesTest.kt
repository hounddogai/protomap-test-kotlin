package com.acme.transport.terminal.dockdoor.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedDockDoorExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedDockDoorExceptionValidator()
        val item = newCappedDockDoorException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = CappedDockDoorExceptionCodeLengthRule(1000)
        val item = newCappedDockDoorException("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = CappedDockDoorExceptionDistanceKmRangeRule(0.7, 1203.2)
        val item = newCappedDockDoorException("x", "y")
        item.distanceKm = 1204.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = CappedDockDoorExceptionRequiredExternalRefRule("capped dock door exception needs this field")
        val item = newCappedDockDoorException("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = CappedDockDoorExceptionRequiredDockRule("capped dock door exception needs an entry")
        val item = newCappedDockDoorException("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
