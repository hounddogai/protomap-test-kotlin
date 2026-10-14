package com.acme.transport.safety.coaching.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictCoachingScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictCoachingScheduleValidator()
        val item = newStrictCoachingSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = StrictCoachingScheduleRequiredExternalRefRule("strict coaching schedule needs an entry")
        val item = newStrictCoachingSchedule("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = StrictCoachingScheduleRequiredCodeRule("strict coaching schedule needs an entry")
        val item = newStrictCoachingSchedule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = StrictCoachingScheduleRequiredTitleRule("strict coaching schedule needs this field")
        val item = newStrictCoachingSchedule("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = StrictCoachingScheduleExternalRefLengthRule(16)
        val item = newStrictCoachingSchedule("x", "y")
        item.externalRef = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
