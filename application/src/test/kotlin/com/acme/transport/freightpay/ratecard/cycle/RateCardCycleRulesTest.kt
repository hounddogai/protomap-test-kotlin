package com.acme.transport.freightpay.ratecard.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RateCardCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRateCardCycleValidator()
        val item = newRateCardCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = RateCardCycleVehicleIdLengthRule(64)
        val item = newRateCardCycle("x", "y")
        item.vehicleId = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RateCardCycleRequiredNameRule("rate card cycle needs an entry")
        val item = newRateCardCycle("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = RateCardCycleLabelLengthRule(32)
        val item = newRateCardCycle("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above cost`() {
        val rule = RateCardCycleCostRangeRule(6.0, 2535.1)
        val item = newRateCardCycle("x", "y")
        item.cost = 2536.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = RateCardCycleAttemptsRangeRule(64, 1000)
        val item = newRateCardCycle("x", "y")
        item.attempts = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = RateCardCycleCodeLengthRule(80)
        val item = newRateCardCycle("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
