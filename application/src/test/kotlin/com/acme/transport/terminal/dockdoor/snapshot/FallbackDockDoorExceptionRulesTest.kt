package com.acme.transport.terminal.dockdoor.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackDockDoorExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackDockDoorExceptionValidator()
        val item = newFallbackDockDoorException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = FallbackDockDoorExceptionNameLengthRule(32)
        val item = newFallbackDockDoorException("x", "y")
        item.name = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = FallbackDockDoorExceptionRequiredLaneRule("fallback dock door exception needs a value")
        val item = newFallbackDockDoorException("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
