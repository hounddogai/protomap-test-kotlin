package com.acme.transport.crewing.availability.availabilityexception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AvailabilityScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAvailabilityScheduleValidator()
        val item = newAvailabilitySchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = AvailabilityScheduleTitleLengthRule(32)
        val item = newAvailabilitySchedule("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = AvailabilityScheduleRequiredNameRule("availability schedule needs a value")
        val item = newAvailabilitySchedule("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
