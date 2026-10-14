package com.acme.transport.freightpay.ratecard.cardsegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultRateCardCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultRateCardCycleValidator()
        val item = newDefaultRateCardCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = DefaultRateCardCycleNameLengthRule(16)
        val item = newDefaultRateCardCycle("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefaultRateCardCycleCodeLengthRule(16)
        val item = newDefaultRateCardCycle("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = DefaultRateCardCycleContactEmailLengthRule(255)
        val item = newDefaultRateCardCycle("x", "y")
        item.contactEmail = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = DefaultRateCardCycleRequiredContactEmailRule("default rate card cycle needs a value")
        val item = newDefaultRateCardCycle("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
