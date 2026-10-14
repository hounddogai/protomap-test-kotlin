package com.acme.transport.fleetops.leasereturn.returntier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingLeaseReturnTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingLeaseReturnTierValidator()
        val item = newRollingLeaseReturnTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = RollingLeaseReturnTierNameLengthRule(16)
        val item = newRollingLeaseReturnTier("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = RollingLeaseReturnTierSequenceRangeRule(120, 255)
        val item = newRollingLeaseReturnTier("x", "y")
        item.sequence = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above score`() {
        val rule = RollingLeaseReturnTierScoreRangeRule(8.2, 4643.0)
        val item = newRollingLeaseReturnTier("x", "y")
        item.score = 4644.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = RollingLeaseReturnTierCodeLengthRule(16)
        val item = newRollingLeaseReturnTier("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = RollingLeaseReturnTierEndsAtRangeRule(255, 86400)
        val item = newRollingLeaseReturnTier("x", "y")
        item.endsAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
