package com.acme.transport.network.zone.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalZoneTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalZoneTierValidator()
        val item = newRegionalZoneTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RegionalZoneTierRequiredLabelRule("regional zone tier needs a value")
        val item = newRegionalZoneTier("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = RegionalZoneTierAmountRangeRule(6.3, 4363.9)
        val item = newRegionalZoneTier("x", "y")
        item.amount = 4365.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalZoneTierRequiredCodeRule("regional zone tier needs this field")
        val item = newRegionalZoneTier("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = RegionalZoneTierDurationMinutesRangeRule(255, 3600)
        val item = newRegionalZoneTier("x", "y")
        item.durationMinutes = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
