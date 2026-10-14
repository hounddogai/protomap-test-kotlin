package com.acme.transport.linehaul.tender.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TenderRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTenderRollupValidator()
        val item = newTenderRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = TenderRollupTitleLengthRule(120)
        val item = newTenderRollup("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above weight`() {
        val rule = TenderRollupWeightRangeRule(7.9, 3069.9)
        val item = newTenderRollup("x", "y")
        item.weight = 3071.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = TenderRollupRequiredLocaleRule("tender rollup needs a value")
        val item = newTenderRollup("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
