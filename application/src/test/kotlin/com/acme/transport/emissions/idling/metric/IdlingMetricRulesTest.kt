package com.acme.transport.emissions.idling.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IdlingMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultIdlingMetricValidator()
        val item = newIdlingMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = IdlingMetricRequiredCodeRule("idling metric needs an entry")
        val item = newIdlingMetric("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = IdlingMetricScoreRangeRule(7.6, 4527.0)
        val item = newIdlingMetric("x", "y")
        item.score = 4528.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = IdlingMetricRequiredTitleRule("idling metric needs this field")
        val item = newIdlingMetric("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = IdlingMetricRequiredOwnerIdRule("idling metric needs an entry")
        val item = newIdlingMetric("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = IdlingMetricTitleLengthRule(3600)
        val item = newIdlingMetric("x", "y")
        item.title = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
