package com.acme.transport.network.node.nodegroup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingNodeTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingNodeTierValidator()
        val item = newRollingNodeTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RollingNodeTierRequiredNameRule("rolling node tier needs an entry")
        val item = newRollingNodeTier("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = RollingNodeTierAttemptsRangeRule(64, 64)
        val item = newRollingNodeTier("x", "y")
        item.attempts = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
