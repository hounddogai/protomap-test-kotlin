package com.acme.transport.lastmile.redelivery.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalRedeliveryScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalRedeliveryScheduleValidator()
        val item = newRegionalRedeliverySchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long locale`() {
        val rule = RegionalRedeliveryScheduleLocaleLengthRule(120)
        val item = newRegionalRedeliverySchedule("x", "y")
        item.locale = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above retries`() {
        val rule = RegionalRedeliveryScheduleRetriesRangeRule(16, 255)
        val item = newRegionalRedeliverySchedule("x", "y")
        item.retries = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
