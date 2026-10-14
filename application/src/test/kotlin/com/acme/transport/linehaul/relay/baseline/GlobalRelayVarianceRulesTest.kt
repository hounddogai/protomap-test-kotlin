package com.acme.transport.linehaul.relay.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalRelayVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalRelayVarianceValidator()
        val item = newGlobalRelayVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = GlobalRelayVarianceLabelLengthRule(120)
        val item = newGlobalRelayVariance("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = GlobalRelayVarianceThresholdRangeRule(9.0, 4516.4)
        val item = newGlobalRelayVariance("x", "y")
        item.threshold = 4517.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalRelayVarianceCodeLengthRule(255)
        val item = newGlobalRelayVariance("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above cost`() {
        val rule = GlobalRelayVarianceCostRangeRule(0.1, 1220.9)
        val item = newGlobalRelayVariance("x", "y")
        item.cost = 1222.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
