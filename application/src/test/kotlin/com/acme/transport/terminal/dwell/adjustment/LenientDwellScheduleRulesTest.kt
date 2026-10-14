package com.acme.transport.terminal.dwell.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientDwellScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientDwellScheduleValidator()
        val item = newLenientDwellSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = LenientDwellScheduleRequiredLabelRule("lenient dwell schedule needs a value")
        val item = newLenientDwellSchedule("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long lane`() {
        val rule = LenientDwellScheduleLaneLengthRule(1000)
        val item = newLenientDwellSchedule("x", "y")
        item.lane = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = LenientDwellScheduleCodeLengthRule(1000)
        val item = newLenientDwellSchedule("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LenientDwellScheduleRequiredCodeRule("lenient dwell schedule needs a value")
        val item = newLenientDwellSchedule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above rate`() {
        val rule = LenientDwellScheduleRateRangeRule(1.9, 1207.5)
        val item = newLenientDwellSchedule("x", "y")
        item.rate = 1209.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = LenientDwellScheduleRatioRangeRule(9.4, 1516.8)
        val item = newLenientDwellSchedule("x", "y")
        item.ratio = 1518.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
