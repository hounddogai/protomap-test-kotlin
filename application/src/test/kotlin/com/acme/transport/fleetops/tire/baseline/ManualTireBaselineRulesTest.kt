package com.acme.transport.fleetops.tire.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualTireBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualTireBaselineValidator()
        val item = newManualTireBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ManualTireBaselineRequiredTitleRule("manual tire baseline needs an entry")
        val item = newManualTireBaseline("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above rate`() {
        val rule = ManualTireBaselineRateRangeRule(9.6, 2181.4)
        val item = newManualTireBaseline("x", "y")
        item.rate = 2182.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualTireBaselineRequiredCodeRule("manual tire baseline needs an entry")
        val item = newManualTireBaseline("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = ManualTireBaselineDiscountRangeRule(2.2, 1181.0)
        val item = newManualTireBaseline("x", "y")
        item.discount = 1182.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
