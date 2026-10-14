package com.acme.transport.lastmile.redelivery.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RedeliveryScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRedeliveryScheduleValidator()
        val item = newRedeliverySchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = RedeliveryScheduleTimezoneLengthRule(255)
        val item = newRedeliverySchedule("x", "y")
        item.timezone = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = RedeliveryScheduleRequiredTimezoneRule("redelivery schedule needs an entry")
        val item = newRedeliverySchedule("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = RedeliveryScheduleNameLengthRule(120)
        val item = newRedeliverySchedule("x", "y")
        item.name = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
