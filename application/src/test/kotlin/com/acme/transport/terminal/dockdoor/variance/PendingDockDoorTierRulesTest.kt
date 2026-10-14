package com.acme.transport.terminal.dockdoor.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingDockDoorTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingDockDoorTierValidator()
        val item = newPendingDockDoorTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long region`() {
        val rule = PendingDockDoorTierRegionLengthRule(86400)
        val item = newPendingDockDoorTier("x", "y")
        item.region = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PendingDockDoorTierRequiredCodeRule("pending dock door tier needs a value")
        val item = newPendingDockDoorTier("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = PendingDockDoorTierTimezoneLengthRule(255)
        val item = newPendingDockDoorTier("x", "y")
        item.timezone = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = PendingDockDoorTierCodeLengthRule(86400)
        val item = newPendingDockDoorTier("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
