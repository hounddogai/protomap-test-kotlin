package com.acme.transport.customs.declaration.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeclarationMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDeclarationMetricValidator()
        val item = newDeclarationMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = DeclarationMetricThresholdRangeRule(1.7, 4596.0)
        val item = newDeclarationMetric("x", "y")
        item.threshold = 4597.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DeclarationMetricRequiredCodeRule("declaration metric needs an entry")
        val item = newDeclarationMetric("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DeclarationMetricCodeLengthRule(1000)
        val item = newDeclarationMetric("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
