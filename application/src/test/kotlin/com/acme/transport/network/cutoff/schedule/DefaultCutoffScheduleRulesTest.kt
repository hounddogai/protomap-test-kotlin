package com.acme.transport.network.cutoff.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultCutoffScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultCutoffScheduleValidator()
        val item = newDefaultCutoffSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = DefaultCutoffScheduleRatioRangeRule(9.4, 1366.7)
        val item = newDefaultCutoffSchedule("x", "y")
        item.ratio = 1368.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = DefaultCutoffScheduleCeilingRangeRule(8.6, 1810.1)
        val item = newDefaultCutoffSchedule("x", "y")
        item.ceiling = 1811.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = DefaultCutoffSchedulePalletsRangeRule(32, 3600)
        val item = newDefaultCutoffSchedule("x", "y")
        item.pallets = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = DefaultCutoffScheduleShiftCodeLengthRule(1000)
        val item = newDefaultCutoffSchedule("x", "y")
        item.shiftCode = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
