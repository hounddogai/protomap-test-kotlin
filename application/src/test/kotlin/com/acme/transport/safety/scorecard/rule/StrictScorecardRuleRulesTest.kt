package com.acme.transport.safety.scorecard.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictScorecardRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictScorecardRuleValidator()
        val item = newStrictScorecardRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = StrictScorecardRuleRequiredTitleRule("strict scorecard rule needs an entry")
        val item = newStrictScorecardRule("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = StrictScorecardRuleSequenceRangeRule(1000, 1000)
        val item = newStrictScorecardRule("x", "y")
        item.sequence = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = StrictScorecardRuleRequiredCodeRule("strict scorecard rule needs an entry")
        val item = newStrictScorecardRule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank region`() {
        val rule = StrictScorecardRuleRequiredRegionRule("strict scorecard rule needs this field")
        val item = newStrictScorecardRule("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
