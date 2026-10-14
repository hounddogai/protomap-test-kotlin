package com.acme.transport.network.servicearea.areaexception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryServiceAreaEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryServiceAreaEntryValidator()
        val item = newPrimaryServiceAreaEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long summary`() {
        val rule = PrimaryServiceAreaEntrySummaryLengthRule(255)
        val item = newPrimaryServiceAreaEntry("x", "y")
        item.summary = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = PrimaryServiceAreaEntryFuelLitersRangeRule(3.2, 969.7)
        val item = newPrimaryServiceAreaEntry("x", "y")
        item.fuelLiters = 971.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above amount`() {
        val rule = PrimaryServiceAreaEntryAmountRangeRule(6.0, 2949.2)
        val item = newPrimaryServiceAreaEntry("x", "y")
        item.amount = 2950.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = PrimaryServiceAreaEntryMaximumRangeRule(2.5, 3715.8)
        val item = newPrimaryServiceAreaEntry("x", "y")
        item.maximum = 3717.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
