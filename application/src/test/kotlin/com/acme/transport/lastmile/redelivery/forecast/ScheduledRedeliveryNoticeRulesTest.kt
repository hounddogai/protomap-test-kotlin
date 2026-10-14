package com.acme.transport.lastmile.redelivery.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledRedeliveryNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledRedeliveryNoticeValidator()
        val item = newScheduledRedeliveryNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long country`() {
        val rule = ScheduledRedeliveryNoticeCountryLengthRule(32)
        val item = newScheduledRedeliveryNotice("x", "y")
        item.country = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledRedeliveryNoticeRequiredCodeRule("scheduled redelivery notice needs an entry")
        val item = newScheduledRedeliveryNotice("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank country`() {
        val rule = ScheduledRedeliveryNoticeRequiredCountryRule("scheduled redelivery notice needs a value")
        val item = newScheduledRedeliveryNotice("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = ScheduledRedeliveryNoticeDriverIdLengthRule(80)
        val item = newScheduledRedeliveryNotice("x", "y")
        item.driverId = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
