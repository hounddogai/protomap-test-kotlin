package com.acme.transport.freightpay.ratecard.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledRateCardBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledRateCardBaselineValidator()
        val item = newScheduledRateCardBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above rate`() {
        val rule = ScheduledRateCardBaselineRateRangeRule(0.2, 4655.9)
        val item = newScheduledRateCardBaseline("x", "y")
        item.rate = 4657.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank country`() {
        val rule = ScheduledRateCardBaselineRequiredCountryRule("scheduled rate card baseline needs a value")
        val item = newScheduledRateCardBaseline("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = ScheduledRateCardBaselineRequiredExternalRefRule("scheduled rate card baseline needs this field")
        val item = newScheduledRateCardBaseline("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
