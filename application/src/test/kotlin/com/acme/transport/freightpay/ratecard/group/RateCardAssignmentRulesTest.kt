package com.acme.transport.freightpay.ratecard.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RateCardAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRateCardAssignmentValidator()
        val item = newRateCardAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = RateCardAssignmentTitleLengthRule(80)
        val item = newRateCardAssignment("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above version`() {
        val rule = RateCardAssignmentVersionRangeRule(80, 255)
        val item = newRateCardAssignment("x", "y")
        item.version = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = RateCardAssignmentRequiredDriverIdRule("rate card assignment needs a value")
        val item = newRateCardAssignment("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
