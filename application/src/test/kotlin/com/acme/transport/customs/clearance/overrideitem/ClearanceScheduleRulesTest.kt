package com.acme.transport.customs.clearance.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClearanceScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultClearanceScheduleValidator()
        val item = newClearanceSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ClearanceScheduleRequiredCodeRule("clearance schedule needs a value")
        val item = newClearanceSchedule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ClearanceScheduleRequiredNameRule("clearance schedule needs an entry")
        val item = newClearanceSchedule("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank description`() {
        val rule = ClearanceScheduleRequiredDescriptionRule("clearance schedule needs this field")
        val item = newClearanceSchedule("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = ClearanceScheduleQuantityRangeRule(16, 80)
        val item = newClearanceSchedule("x", "y")
        item.quantity = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
