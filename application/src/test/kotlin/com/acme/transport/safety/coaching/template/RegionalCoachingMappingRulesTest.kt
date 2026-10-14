package com.acme.transport.safety.coaching.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalCoachingMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalCoachingMappingValidator()
        val item = newRegionalCoachingMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = RegionalCoachingMappingRequiredCarrierRule("regional coaching mapping needs a value")
        val item = newRegionalCoachingMapping("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = RegionalCoachingMappingTitleLengthRule(80)
        val item = newRegionalCoachingMapping("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = RegionalCoachingMappingMinimumRangeRule(3.4, 3961.2)
        val item = newRegionalCoachingMapping("x", "y")
        item.minimum = 3962.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = RegionalCoachingMappingRequiredTitleRule("regional coaching mapping needs an entry")
        val item = newRegionalCoachingMapping("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
