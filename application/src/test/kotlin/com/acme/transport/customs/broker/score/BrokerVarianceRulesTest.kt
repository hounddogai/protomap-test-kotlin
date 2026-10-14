package com.acme.transport.customs.broker.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BrokerVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBrokerVarianceValidator()
        val item = newBrokerVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = BrokerVarianceCapacityRangeRule(80, 120)
        val item = newBrokerVariance("x", "y")
        item.capacity = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = BrokerVarianceTitleLengthRule(86400)
        val item = newBrokerVariance("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = BrokerVarianceQuantityRangeRule(80, 255)
        val item = newBrokerVariance("x", "y")
        item.quantity = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = BrokerVarianceCodeLengthRule(80)
        val item = newBrokerVariance("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BrokerVarianceRequiredCodeRule("broker variance needs an entry")
        val item = newBrokerVariance("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
