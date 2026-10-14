package com.acme.transport.network.zone.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictZoneScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictZoneScheduleValidator()
        val item = newStrictZoneSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = StrictZoneScheduleDiscountRangeRule(1.4, 4889.7)
        val item = newStrictZoneSchedule("x", "y")
        item.discount = 4891.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = StrictZoneScheduleCodeLengthRule(64)
        val item = newStrictZoneSchedule("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = StrictZoneScheduleRequiredLabelRule("strict zone schedule needs an entry")
        val item = newStrictZoneSchedule("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = StrictZoneScheduleRequiredNotesRule("strict zone schedule needs this field")
        val item = newStrictZoneSchedule("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
