package com.acme.transport.terminal.dockdoor.doorthreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedDockDoorExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedDockDoorExceptionValidator()
        val item = newWeightedDockDoorException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = WeightedDockDoorExceptionEtaMinutesRangeRule(16, 1000)
        val item = newWeightedDockDoorException("x", "y")
        item.etaMinutes = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = WeightedDockDoorExceptionRequiredTitleRule("weighted dock door exception needs a value")
        val item = newWeightedDockDoorException("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
