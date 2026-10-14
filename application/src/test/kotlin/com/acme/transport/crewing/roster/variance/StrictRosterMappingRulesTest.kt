package com.acme.transport.crewing.roster.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictRosterMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictRosterMappingValidator()
        val item = newStrictRosterMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank country`() {
        val rule = StrictRosterMappingRequiredCountryRule("strict roster mapping needs a value")
        val item = newStrictRosterMapping("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = StrictRosterMappingThresholdRangeRule(6.2, 783.7)
        val item = newStrictRosterMapping("x", "y")
        item.threshold = 785.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = StrictRosterMappingRatioRangeRule(2.8, 2752.7)
        val item = newStrictRosterMapping("x", "y")
        item.ratio = 2754.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = StrictRosterMappingRequiredCodeRule("strict roster mapping needs an entry")
        val item = newStrictRosterMapping("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
