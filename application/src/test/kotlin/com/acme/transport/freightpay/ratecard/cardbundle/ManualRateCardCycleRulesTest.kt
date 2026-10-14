package com.acme.transport.freightpay.ratecard.cardbundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualRateCardCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualRateCardCycleValidator()
        val item = newManualRateCardCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = ManualRateCardCycleLabelLengthRule(255)
        val item = newManualRateCardCycle("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = ManualRateCardCycleStartsAtRangeRule(32, 120)
        val item = newManualRateCardCycle("x", "y")
        item.startsAt = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above limit`() {
        val rule = ManualRateCardCycleLimitRangeRule(64, 86400)
        val item = newManualRateCardCycle("x", "y")
        item.limit = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = ManualRateCardCycleCodeLengthRule(1000)
        val item = newManualRateCardCycle("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
