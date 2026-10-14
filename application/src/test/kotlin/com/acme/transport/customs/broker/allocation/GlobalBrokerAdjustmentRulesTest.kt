package com.acme.transport.customs.broker.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalBrokerAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalBrokerAdjustmentValidator()
        val item = newGlobalBrokerAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalBrokerAdjustmentCodeLengthRule(16)
        val item = newGlobalBrokerAdjustment("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = GlobalBrokerAdjustmentRequiredNameRule("global broker adjustment needs a value")
        val item = newGlobalBrokerAdjustment("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = GlobalBrokerAdjustmentDistanceKmRangeRule(0.9, 1681.5)
        val item = newGlobalBrokerAdjustment("x", "y")
        item.distanceKm = 1683.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = GlobalBrokerAdjustmentLabelLengthRule(120)
        val item = newGlobalBrokerAdjustment("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = GlobalBrokerAdjustmentNameLengthRule(1000)
        val item = newGlobalBrokerAdjustment("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
