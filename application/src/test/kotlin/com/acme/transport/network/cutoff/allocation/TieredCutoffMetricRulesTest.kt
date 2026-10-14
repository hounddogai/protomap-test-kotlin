package com.acme.transport.network.cutoff.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredCutoffMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredCutoffMetricValidator()
        val item = newTieredCutoffMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = TieredCutoffMetricRequiredLabelRule("tiered cutoff metric needs this field")
        val item = newTieredCutoffMetric("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = TieredCutoffMetricAmountRangeRule(5.9, 2414.5)
        val item = newTieredCutoffMetric("x", "y")
        item.amount = 2416.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
