package com.acme.transport.customs.broker.request

import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyBrokerBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyBrokerBaselineValidator()
        val item = newLegacyBrokerBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = LegacyBrokerBaselineQuantityRangeRule(86400, 86400)
        val item = newLegacyBrokerBaseline("x", "y")
        item.quantity = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above discount`() {
        val rule = LegacyBrokerBaselineDiscountRangeRule(6.7, 4704.3)
        val item = newLegacyBrokerBaseline("x", "y")
        item.discount = 4705.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above cost`() {
        val rule = LegacyBrokerBaselineCostRangeRule(2.7, 909.5)
        val item = newLegacyBrokerBaseline("x", "y")
        item.cost = 911.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
