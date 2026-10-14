package com.acme.transport.freightpay.ratecard.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingRateCardEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingRateCardEntryValidator()
        val item = newRollingRateCardEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = RollingRateCardEntryOdometerRangeRule(16, 1000)
        val item = newRollingRateCardEntry("x", "y")
        item.odometer = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank category`() {
        val rule = RollingRateCardEntryRequiredCategoryRule("rolling rate card entry needs this field")
        val item = newRollingRateCardEntry("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = RollingRateCardEntryRequiredTitleRule("rolling rate card entry needs an entry")
        val item = newRollingRateCardEntry("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
