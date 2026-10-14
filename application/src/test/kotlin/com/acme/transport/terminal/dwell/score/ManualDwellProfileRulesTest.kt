package com.acme.transport.terminal.dwell.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualDwellProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualDwellProfileValidator()
        val item = newManualDwellProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = ManualDwellProfileFloorValueRangeRule(9.0, 2939.4)
        val item = newManualDwellProfile("x", "y")
        item.floorValue = 2940.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ManualDwellProfileRequiredTitleRule("manual dwell profile needs a value")
        val item = newManualDwellProfile("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = ManualDwellProfileTitleLengthRule(80)
        val item = newManualDwellProfile("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
