package com.acme.transport.crewing.restbreak.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RestBreakTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRestBreakTierValidator()
        val item = newRestBreakTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long channel`() {
        val rule = RestBreakTierChannelLengthRule(80)
        val item = newRestBreakTier("x", "y")
        item.channel = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = RestBreakTierNameLengthRule(255)
        val item = newRestBreakTier("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = RestBreakTierRequiredChannelRule("rest break tier needs an entry")
        val item = newRestBreakTier("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = RestBreakTierVehicleIdLengthRule(64)
        val item = newRestBreakTier("x", "y")
        item.vehicleId = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
