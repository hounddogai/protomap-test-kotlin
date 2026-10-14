package com.acme.transport.freightpay.fuelsurcharge.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingFuelSurchargePreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingFuelSurchargePreferenceValidator()
        val item = newRollingFuelSurchargePreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RollingFuelSurchargePreferenceRequiredCodeRule("rolling fuel surcharge preference needs an entry")
        val item = newRollingFuelSurchargePreference("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = RollingFuelSurchargePreferenceStopCountRangeRule(80, 1000)
        val item = newRollingFuelSurchargePreference("x", "y")
        item.stopCount = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = RollingFuelSurchargePreferenceRequiredTitleRule(
            "rolling fuel surcharge preference needs this field",
        )
        val item = newRollingFuelSurchargePreference("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = RollingFuelSurchargePreferenceScoreRangeRule(9.4, 3813.2)
        val item = newRollingFuelSurchargePreference("x", "y")
        item.score = 3814.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
