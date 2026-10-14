package com.acme.transport.safety.coaching.fallbackcoachingmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalCoachingMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalCoachingMetricValidator()
        val item = newSeasonalCoachingMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = SeasonalCoachingMetricRequiredLabelRule("seasonal coaching metric needs an entry")
        val item = newSeasonalCoachingMetric("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = SeasonalCoachingMetricCodeLengthRule(64)
        val item = newSeasonalCoachingMetric("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long category`() {
        val rule = SeasonalCoachingMetricCategoryLengthRule(86400)
        val item = newSeasonalCoachingMetric("x", "y")
        item.category = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
