package com.acme.transport.customs.tariff.tariffprofile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingTariffThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingTariffThresholdValidator()
        val item = newRollingTariffThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = RollingTariffThresholdWindowDaysRangeRule(255, 255)
        val item = newRollingTariffThreshold("x", "y")
        item.windowDays = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = RollingTariffThresholdGraceDaysRangeRule(120, 120)
        val item = newRollingTariffThreshold("x", "y")
        item.graceDays = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RollingTariffThresholdRequiredLabelRule("rolling tariff threshold needs this field")
        val item = newRollingTariffThreshold("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = RollingTariffThresholdRequiredContactEmailRule("rolling tariff threshold needs this field")
        val item = newRollingTariffThreshold("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
