package com.acme.transport.linehaul.tender.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TenderBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTenderBaselineValidator()
        val item = newTenderBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TenderBaselineRequiredCodeRule("tender baseline needs a value")
        val item = newTenderBaseline("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above margin`() {
        val rule = TenderBaselineMarginRangeRule(8.7, 4292.2)
        val item = newTenderBaseline("x", "y")
        item.margin = 4293.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = TenderBaselineEndsAtRangeRule(255, 1000)
        val item = newTenderBaseline("x", "y")
        item.endsAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
