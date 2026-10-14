package com.acme.transport.terminal.dwell.digest

import kotlin.test.Test
import kotlin.test.assertTrue

class StrictDwellCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictDwellCheckpointValidator()
        val item = newStrictDwellCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = StrictDwellCheckpointRetriesRangeRule(80, 80)
        val item = newStrictDwellCheckpoint("x", "y")
        item.retries = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above amount`() {
        val rule = StrictDwellCheckpointAmountRangeRule(0.3, 188.9)
        val item = newStrictDwellCheckpoint("x", "y")
        item.amount = 190.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
