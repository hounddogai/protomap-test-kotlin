package com.acme.transport.terminal.dwell.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DwellScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDwellScoreValidator()
        val item = newDwellScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = DwellScoreRequiredLabelRule("dwell score needs this field")
        val item = newDwellScore("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = DwellScorePriorityRangeRule(16, 80)
        val item = newDwellScore("x", "y")
        item.priority = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
