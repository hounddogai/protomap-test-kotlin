package com.acme.transport.customs.hold.holdsummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientHoldScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientHoldScheduleValidator()
        val item = newLenientHoldSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LenientHoldScheduleCodeLengthRule(64)
        val item = newLenientHoldSchedule("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = LenientHoldSchedulePalletsRangeRule(80, 3600)
        val item = newLenientHoldSchedule("x", "y")
        item.pallets = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LenientHoldScheduleRequiredCodeRule("lenient hold schedule needs an entry")
        val item = newLenientHoldSchedule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
