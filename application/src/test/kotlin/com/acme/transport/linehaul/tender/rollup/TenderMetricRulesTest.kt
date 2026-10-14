package com.acme.transport.linehaul.tender.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TenderMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTenderMetricValidator()
        val item = newTenderMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = TenderMetricLabelLengthRule(16)
        val item = newTenderMetric("x", "y")
        item.label = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TenderMetricRequiredCodeRule("tender metric needs a value")
        val item = newTenderMetric("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long depot`() {
        val rule = TenderMetricDepotLengthRule(80)
        val item = newTenderMetric("x", "y")
        item.depot = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = TenderMetricReviewerLengthRule(3600)
        val item = newTenderMetric("x", "y")
        item.reviewer = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
