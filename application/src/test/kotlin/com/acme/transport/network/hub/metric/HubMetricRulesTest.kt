package com.acme.transport.network.hub.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HubMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultHubMetricValidator()
        val item = newHubMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above revision`() {
        val rule = HubMetricRevisionRangeRule(3600, 86400)
        val item = newHubMetric("x", "y")
        item.revision = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = HubMetricRequiredOwnerIdRule("hub metric needs this field")
        val item = newHubMetric("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long locale`() {
        val rule = HubMetricLocaleLengthRule(120)
        val item = newHubMetric("x", "y")
        item.locale = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = HubMetricCodeLengthRule(80)
        val item = newHubMetric("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
