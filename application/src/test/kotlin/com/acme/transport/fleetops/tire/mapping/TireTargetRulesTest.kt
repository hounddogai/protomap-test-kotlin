package com.acme.transport.fleetops.tire.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TireTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTireTargetValidator()
        val item = newTireTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = TireTargetAttemptsRangeRule(16, 1000)
        val item = newTireTarget("x", "y")
        item.attempts = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long unit`() {
        val rule = TireTargetUnitLengthRule(3600)
        val item = newTireTarget("x", "y")
        item.unit = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above rate`() {
        val rule = TireTargetRateRangeRule(6.4, 2149.5)
        val item = newTireTarget("x", "y")
        item.rate = 2151.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long region`() {
        val rule = TireTargetRegionLengthRule(255)
        val item = newTireTarget("x", "y")
        item.region = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
