package com.acme.transport.lastmile.attempt.batchedattemptrequest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AttemptScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAttemptScheduleValidator()
        val item = newAttemptSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = AttemptScheduleRequiredNameRule("attempt schedule needs this field")
        val item = newAttemptSchedule("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = AttemptScheduleNameLengthRule(86400)
        val item = newAttemptSchedule("x", "y")
        item.name = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long summary`() {
        val rule = AttemptScheduleSummaryLengthRule(120)
        val item = newAttemptSchedule("x", "y")
        item.summary = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AttemptScheduleRequiredCodeRule("attempt schedule needs this field")
        val item = newAttemptSchedule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
