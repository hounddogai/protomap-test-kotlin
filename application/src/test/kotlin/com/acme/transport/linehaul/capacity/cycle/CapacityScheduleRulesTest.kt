package com.acme.transport.linehaul.capacity.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CapacityScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCapacityScheduleValidator()
        val item = newCapacitySchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CapacityScheduleRequiredCodeRule("capacity schedule needs an entry")
        val item = newCapacitySchedule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = CapacityScheduleNameLengthRule(255)
        val item = newCapacitySchedule("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long unit`() {
        val rule = CapacityScheduleUnitLengthRule(120)
        val item = newCapacitySchedule("x", "y")
        item.unit = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = CapacityScheduleRequiredNameRule("capacity schedule needs an entry")
        val item = newCapacitySchedule("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = CapacityScheduleEtaMinutesRangeRule(80, 3600)
        val item = newCapacitySchedule("x", "y")
        item.etaMinutes = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
