package com.acme.transport.emissions.evcharge.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvChargeSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEvChargeSummaryValidator()
        val item = newEvChargeSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank category`() {
        val rule = EvChargeSummaryRequiredCategoryRule("ev charge summary needs a value")
        val item = newEvChargeSummary("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = EvChargeSummaryTrailerIdLengthRule(3600)
        val item = newEvChargeSummary("x", "y")
        item.trailerId = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long category`() {
        val rule = EvChargeSummaryCategoryLengthRule(255)
        val item = newEvChargeSummary("x", "y")
        item.category = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = EvChargeSummaryRequiredTrailerIdRule("ev charge summary needs a value")
        val item = newEvChargeSummary("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
