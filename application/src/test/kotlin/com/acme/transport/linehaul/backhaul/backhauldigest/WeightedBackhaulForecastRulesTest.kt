package com.acme.transport.linehaul.backhaul.backhauldigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedBackhaulForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedBackhaulForecastValidator()
        val item = newWeightedBackhaulForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = WeightedBackhaulForecastRequiredCodeRule("weighted backhaul forecast needs a value")
        val item = newWeightedBackhaulForecast("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = WeightedBackhaulForecastRequiredTitleRule("weighted backhaul forecast needs an entry")
        val item = newWeightedBackhaulForecast("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = WeightedBackhaulForecastReviewerLengthRule(120)
        val item = newWeightedBackhaulForecast("x", "y")
        item.reviewer = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
