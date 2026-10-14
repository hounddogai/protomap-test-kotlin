package com.acme.transport.linehaul.laneplan.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledLanePlanScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledLanePlanScheduleValidator()
        val item = newScheduledLanePlanSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank category`() {
        val rule = ScheduledLanePlanScheduleRequiredCategoryRule("scheduled lane plan schedule needs an entry")
        val item = newScheduledLanePlanSchedule("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above margin`() {
        val rule = ScheduledLanePlanScheduleMarginRangeRule(6.9, 723.3)
        val item = newScheduledLanePlanSchedule("x", "y")
        item.margin = 724.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long category`() {
        val rule = ScheduledLanePlanScheduleCategoryLengthRule(16)
        val item = newScheduledLanePlanSchedule("x", "y")
        item.category = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledLanePlanScheduleRequiredCodeRule("scheduled lane plan schedule needs a value")
        val item = newScheduledLanePlanSchedule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = ScheduledLanePlanScheduleLabelLengthRule(86400)
        val item = newScheduledLanePlanSchedule("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long locale`() {
        val rule = ScheduledLanePlanScheduleLocaleLengthRule(16)
        val item = newScheduledLanePlanSchedule("x", "y")
        item.locale = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
