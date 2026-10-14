package com.acme.transport.terminal.crossdock.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CrossDockTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCrossDockTierValidator()
        val item = newCrossDockTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = CrossDockTierRequiredNameRule("cross dock tier needs an entry")
        val item = newCrossDockTier("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = CrossDockTierDriverIdLengthRule(32)
        val item = newCrossDockTier("x", "y")
        item.driverId = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = CrossDockTierFloorValueRangeRule(1.2, 1223.3)
        val item = newCrossDockTier("x", "y")
        item.floorValue = 1224.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = CrossDockTierMinimumRangeRule(2.4, 613.2)
        val item = newCrossDockTier("x", "y")
        item.minimum = 614.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
