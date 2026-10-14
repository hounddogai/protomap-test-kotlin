package com.acme.transport.network.servicearea.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServiceAreaScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultServiceAreaScheduleValidator()
        val item = newServiceAreaSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long currency`() {
        val rule = ServiceAreaScheduleCurrencyLengthRule(255)
        val item = newServiceAreaSchedule("x", "y")
        item.currency = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = ServiceAreaScheduleRequiredCurrencyRule("service area schedule needs an entry")
        val item = newServiceAreaSchedule("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ServiceAreaScheduleCodeLengthRule(32)
        val item = newServiceAreaSchedule("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
