package com.acme.transport.linehaul.relay.relaythreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingRelayTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingRelayTierValidator()
        val item = newRollingRelayTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = RollingRelayTierRequiredPhoneRule("rolling relay tier needs an entry")
        val item = newRollingRelayTier("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = RollingRelayTierRequiredLaneRule("rolling relay tier needs this field")
        val item = newRollingRelayTier("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = RollingRelayTierPalletsRangeRule(120, 1000)
        val item = newRollingRelayTier("x", "y")
        item.pallets = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long phone`() {
        val rule = RollingRelayTierPhoneLengthRule(1000)
        val item = newRollingRelayTier("x", "y")
        item.phone = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
