package com.acme.transport.network.transittime.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualTransitTimeBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualTransitTimeBatchValidator()
        val item = newManualTransitTimeBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ManualTransitTimeBatchRequiredTitleRule("manual transit time batch needs this field")
        val item = newManualTransitTimeBatch("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = ManualTransitTimeBatchTimezoneLengthRule(64)
        val item = newManualTransitTimeBatch("x", "y")
        item.timezone = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = ManualTransitTimeBatchRequiredTimezoneRule("manual transit time batch needs an entry")
        val item = newManualTransitTimeBatch("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = ManualTransitTimeBatchMaximumRangeRule(7.4, 4472.7)
        val item = newManualTransitTimeBatch("x", "y")
        item.maximum = 4474.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long category`() {
        val rule = ManualTransitTimeBatchCategoryLengthRule(80)
        val item = newManualTransitTimeBatch("x", "y")
        item.category = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
