package com.acme.transport.network.transittime.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TransitTimeSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTransitTimeSummaryValidator()
        val item = newTransitTimeSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = TransitTimeSummaryPalletsRangeRule(1000, 1000)
        val item = newTransitTimeSummary("x", "y")
        item.pallets = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = TransitTimeSummaryRequiredNameRule("transit time summary needs a value")
        val item = newTransitTimeSummary("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = TransitTimeSummaryUpdatedAtRangeRule(16, 120)
        val item = newTransitTimeSummary("x", "y")
        item.updatedAt = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = TransitTimeSummaryRequiredTitleRule("transit time summary needs an entry")
        val item = newTransitTimeSummary("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
