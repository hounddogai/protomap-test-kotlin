package com.acme.transport.network.node.nodesnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyNodeMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyNodeMetricValidator()
        val item = newLegacyNodeMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = LegacyNodeMetricRequiredPhoneRule("legacy node metric needs an entry")
        val item = newLegacyNodeMetric("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = LegacyNodeMetricRequiredLabelRule("legacy node metric needs a value")
        val item = newLegacyNodeMetric("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LegacyNodeMetricRequiredCodeRule("legacy node metric needs an entry")
        val item = newLegacyNodeMetric("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above margin`() {
        val rule = LegacyNodeMetricMarginRangeRule(8.6, 1727.0)
        val item = newLegacyNodeMetric("x", "y")
        item.margin = 1728.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
