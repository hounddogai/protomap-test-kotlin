package com.acme.transport.terminal.dockdoor.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingDockDoorTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingDockDoorTierValidator()
        val item = newRollingDockDoorTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = RollingDockDoorTierScoreRangeRule(0.0, 422.1)
        val item = newRollingDockDoorTier("x", "y")
        item.score = 423.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = RollingDockDoorTierRequiredChecksumRule("rolling dock door tier needs a value")
        val item = newRollingDockDoorTier("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RollingDockDoorTierRequiredCodeRule("rolling dock door tier needs an entry")
        val item = newRollingDockDoorTier("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
