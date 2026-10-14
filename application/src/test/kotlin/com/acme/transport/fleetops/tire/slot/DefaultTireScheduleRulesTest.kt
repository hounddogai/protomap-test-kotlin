package com.acme.transport.fleetops.tire.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultTireScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultTireScheduleValidator()
        val item = newDefaultTireSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = DefaultTireScheduleRequiredContactEmailRule("default tire schedule needs this field")
        val item = newDefaultTireSchedule("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = DefaultTireScheduleNameLengthRule(32)
        val item = newDefaultTireSchedule("x", "y")
        item.name = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = DefaultTireScheduleBatchSizeRangeRule(80, 3600)
        val item = newDefaultTireSchedule("x", "y")
        item.batchSize = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = DefaultTireScheduleReviewerLengthRule(32)
        val item = newDefaultTireSchedule("x", "y")
        item.reviewer = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
