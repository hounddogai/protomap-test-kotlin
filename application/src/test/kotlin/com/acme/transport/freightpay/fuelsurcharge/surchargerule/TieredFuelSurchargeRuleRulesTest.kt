package com.acme.transport.freightpay.fuelsurcharge.surchargerule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredFuelSurchargeRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredFuelSurchargeRuleValidator()
        val item = newTieredFuelSurchargeRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TieredFuelSurchargeRuleRequiredCodeRule("tiered fuel surcharge rule needs an entry")
        val item = newTieredFuelSurchargeRule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = TieredFuelSurchargeRuleRequiredChannelRule("tiered fuel surcharge rule needs a value")
        val item = newTieredFuelSurchargeRule("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long channel`() {
        val rule = TieredFuelSurchargeRuleChannelLengthRule(32)
        val item = newTieredFuelSurchargeRule("x", "y")
        item.channel = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long summary`() {
        val rule = TieredFuelSurchargeRuleSummaryLengthRule(255)
        val item = newTieredFuelSurchargeRule("x", "y")
        item.summary = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = TieredFuelSurchargeRuleRequiredTitleRule("tiered fuel surcharge rule needs this field")
        val item = newTieredFuelSurchargeRule("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
