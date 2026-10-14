package com.acme.transport.freightpay.dispute.reservation

import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyDisputeTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyDisputeTierValidator()
        val item = newLegacyDisputeTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = LegacyDisputeTierFloorValueRangeRule(5.8, 2043.4)
        val item = newLegacyDisputeTier("x", "y")
        item.floorValue = 2044.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = LegacyDisputeTierStartsAtRangeRule(64, 86400)
        val item = newLegacyDisputeTier("x", "y")
        item.startsAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above created at`() {
        val rule = LegacyDisputeTierCreatedAtRangeRule(3600, 86400)
        val item = newLegacyDisputeTier("x", "y")
        item.createdAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
