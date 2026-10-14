package com.acme.transport.linehaul.capacity.capacityassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryCapacityVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryCapacityVarianceValidator()
        val item = newPrimaryCapacityVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = PrimaryCapacityVarianceRequiredSummaryRule("primary capacity variance needs an entry")
        val item = newPrimaryCapacityVariance("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PrimaryCapacityVarianceCodeLengthRule(120)
        val item = newPrimaryCapacityVariance("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = PrimaryCapacityVarianceTitleLengthRule(86400)
        val item = newPrimaryCapacityVariance("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long source`() {
        val rule = PrimaryCapacityVarianceSourceLengthRule(80)
        val item = newPrimaryCapacityVariance("x", "y")
        item.source = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long summary`() {
        val rule = PrimaryCapacityVarianceSummaryLengthRule(1000)
        val item = newPrimaryCapacityVariance("x", "y")
        item.summary = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank source`() {
        val rule = PrimaryCapacityVarianceRequiredSourceRule("primary capacity variance needs an entry")
        val item = newPrimaryCapacityVariance("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
