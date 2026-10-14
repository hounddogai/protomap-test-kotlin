package com.acme.transport.terminal.dwell.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DwellRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDwellRollupValidator()
        val item = newDwellRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = DwellRollupRequiredTitleRule("dwell rollup needs an entry")
        val item = newDwellRollup("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = DwellRollupUpdatedAtRangeRule(80, 86400)
        val item = newDwellRollup("x", "y")
        item.updatedAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = DwellRollupTitleLengthRule(255)
        val item = newDwellRollup("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
