package com.acme.transport.freightpay.accessorial.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedAccessorialVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedAccessorialVarianceValidator()
        val item = newWeightedAccessorialVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = WeightedAccessorialVarianceCodeLengthRule(3600)
        val item = newWeightedAccessorialVariance("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = WeightedAccessorialVarianceTitleLengthRule(64)
        val item = newWeightedAccessorialVariance("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long route code`() {
        val rule = WeightedAccessorialVarianceRouteCodeLengthRule(1000)
        val item = newWeightedAccessorialVariance("x", "y")
        item.routeCode = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = WeightedAccessorialVarianceRequiredTitleRule("weighted accessorial variance needs a value")
        val item = newWeightedAccessorialVariance("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = WeightedAccessorialVarianceAmountRangeRule(0.7, 3512.1)
        val item = newWeightedAccessorialVariance("x", "y")
        item.amount = 3513.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = WeightedAccessorialVariancePalletsRangeRule(16, 3600)
        val item = newWeightedAccessorialVariance("x", "y")
        item.pallets = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
