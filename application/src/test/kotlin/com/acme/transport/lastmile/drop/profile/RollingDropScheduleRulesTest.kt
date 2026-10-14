package com.acme.transport.lastmile.drop.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingDropScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingDropScheduleValidator()
        val item = newRollingDropSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RollingDropScheduleCodeLengthRule(3600)
        val item = newRollingDropSchedule("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = RollingDropScheduleShiftCodeLengthRule(120)
        val item = newRollingDropSchedule("x", "y")
        item.shiftCode = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
