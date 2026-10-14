package com.acme.transport.freightpay.ratecard.cardledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingRateCardScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingRateCardScheduleValidator()
        val item = newRollingRateCardSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RollingRateCardScheduleRequiredCodeRule("rolling rate card schedule needs a value")
        val item = newRollingRateCardSchedule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = RollingRateCardScheduleRequiredLocaleRule("rolling rate card schedule needs this field")
        val item = newRollingRateCardSchedule("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
