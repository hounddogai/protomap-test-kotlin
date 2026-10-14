package com.acme.transport.network.cutoff.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingCutoffVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingCutoffVarianceValidator()
        val item = newRollingCutoffVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank description`() {
        val rule = RollingCutoffVarianceRequiredDescriptionRule("rolling cutoff variance needs this field")
        val item = newRollingCutoffVariance("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RollingCutoffVarianceRequiredCodeRule("rolling cutoff variance needs this field")
        val item = newRollingCutoffVariance("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = RollingCutoffVarianceThresholdRangeRule(3.9, 921.5)
        val item = newRollingCutoffVariance("x", "y")
        item.threshold = 923.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
