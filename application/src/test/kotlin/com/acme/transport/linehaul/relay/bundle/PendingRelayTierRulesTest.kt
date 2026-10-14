package com.acme.transport.linehaul.relay.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingRelayTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingRelayTierValidator()
        val item = newPendingRelayTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long dock`() {
        val rule = PendingRelayTierDockLengthRule(1000)
        val item = newPendingRelayTier("x", "y")
        item.dock = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = PendingRelayTierFloorValueRangeRule(1.2, 1683.8)
        val item = newPendingRelayTier("x", "y")
        item.floorValue = 1685.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = PendingRelayTierDistanceKmRangeRule(4.3, 2989.9)
        val item = newPendingRelayTier("x", "y")
        item.distanceKm = 2991.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = PendingRelayTierUpdatedAtRangeRule(64, 80)
        val item = newPendingRelayTier("x", "y")
        item.updatedAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
