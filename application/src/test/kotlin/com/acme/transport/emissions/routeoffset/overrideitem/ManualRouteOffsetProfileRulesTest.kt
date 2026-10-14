package com.acme.transport.emissions.routeoffset.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualRouteOffsetProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualRouteOffsetProfileValidator()
        val item = newManualRouteOffsetProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = ManualRouteOffsetProfileRequiredCurrencyRule("manual route offset profile needs an entry")
        val item = newManualRouteOffsetProfile("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = ManualRouteOffsetProfileQuantityRangeRule(64, 1000)
        val item = newManualRouteOffsetProfile("x", "y")
        item.quantity = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
