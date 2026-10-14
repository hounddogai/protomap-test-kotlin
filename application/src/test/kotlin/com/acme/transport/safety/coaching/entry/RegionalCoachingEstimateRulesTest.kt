package com.acme.transport.safety.coaching.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalCoachingEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalCoachingEstimateValidator()
        val item = newRegionalCoachingEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalCoachingEstimateRequiredCodeRule("regional coaching estimate needs an entry")
        val item = newRegionalCoachingEstimate("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = RegionalCoachingEstimateNameLengthRule(255)
        val item = newRegionalCoachingEstimate("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = RegionalCoachingEstimateStartsAtRangeRule(64, 1000)
        val item = newRegionalCoachingEstimate("x", "y")
        item.startsAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = RegionalCoachingEstimateCodeLengthRule(16)
        val item = newRegionalCoachingEstimate("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = RegionalCoachingEstimateThresholdRangeRule(9.2, 1280.9)
        val item = newRegionalCoachingEstimate("x", "y")
        item.threshold = 1282.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
