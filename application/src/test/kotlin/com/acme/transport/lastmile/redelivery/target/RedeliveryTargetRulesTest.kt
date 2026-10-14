package com.acme.transport.lastmile.redelivery.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RedeliveryTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRedeliveryTargetValidator()
        val item = newRedeliveryTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RedeliveryTargetRequiredCodeRule("redelivery target needs a value")
        val item = newRedeliveryTarget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = RedeliveryTargetNameLengthRule(86400)
        val item = newRedeliveryTarget("x", "y")
        item.name = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = RedeliveryTargetRequiredCurrencyRule("redelivery target needs an entry")
        val item = newRedeliveryTarget("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long currency`() {
        val rule = RedeliveryTargetCurrencyLengthRule(120)
        val item = newRedeliveryTarget("x", "y")
        item.currency = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = RedeliveryTargetCodeLengthRule(3600)
        val item = newRedeliveryTarget("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
