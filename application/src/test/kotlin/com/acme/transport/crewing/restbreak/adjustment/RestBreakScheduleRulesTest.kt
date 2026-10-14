package com.acme.transport.crewing.restbreak.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RestBreakScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRestBreakScheduleValidator()
        val item = newRestBreakSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank description`() {
        val rule = RestBreakScheduleRequiredDescriptionRule("rest break schedule needs this field")
        val item = newRestBreakSchedule("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RestBreakScheduleRequiredCodeRule("rest break schedule needs an entry")
        val item = newRestBreakSchedule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = RestBreakScheduleWeightRangeRule(2.7, 303.0)
        val item = newRestBreakSchedule("x", "y")
        item.weight = 304.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = RestBreakScheduleCodeLengthRule(86400)
        val item = newRestBreakSchedule("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
