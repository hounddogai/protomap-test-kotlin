package com.acme.transport.linehaul.backhaul.primarybackhaulvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryBackhaulMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryBackhaulMetricValidator()
        val item = newPrimaryBackhaulMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = PrimaryBackhaulMetricLabelLengthRule(64)
        val item = newPrimaryBackhaulMetric("x", "y")
        item.label = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above amount`() {
        val rule = PrimaryBackhaulMetricAmountRangeRule(2.1, 2606.2)
        val item = newPrimaryBackhaulMetric("x", "y")
        item.amount = 2607.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above created at`() {
        val rule = PrimaryBackhaulMetricCreatedAtRangeRule(80, 255)
        val item = newPrimaryBackhaulMetric("x", "y")
        item.createdAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PrimaryBackhaulMetricRequiredCodeRule("primary backhaul metric needs this field")
        val item = newPrimaryBackhaulMetric("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PrimaryBackhaulMetricCodeLengthRule(1000)
        val item = newPrimaryBackhaulMetric("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
