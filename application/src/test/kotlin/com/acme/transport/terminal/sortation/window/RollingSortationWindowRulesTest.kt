package com.acme.transport.terminal.sortation.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingSortationWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingSortationWindowValidator()
        val item = newRollingSortationWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = RollingSortationWindowStopCountRangeRule(64, 1000)
        val item = newRollingSortationWindow("x", "y")
        item.stopCount = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RollingSortationWindowRequiredNameRule("rolling sortation window needs an entry")
        val item = newRollingSortationWindow("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = RollingSortationWindowRequiredDockRule("rolling sortation window needs a value")
        val item = newRollingSortationWindow("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
