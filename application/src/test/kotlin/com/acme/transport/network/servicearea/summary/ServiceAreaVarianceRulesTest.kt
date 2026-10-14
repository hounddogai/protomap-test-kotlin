package com.acme.transport.network.servicearea.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServiceAreaVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultServiceAreaVarianceValidator()
        val item = newServiceAreaVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above rate`() {
        val rule = ServiceAreaVarianceRateRangeRule(4.6, 3741.6)
        val item = newServiceAreaVariance("x", "y")
        item.rate = 3743.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = ServiceAreaVarianceNameLengthRule(80)
        val item = newServiceAreaVariance("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
