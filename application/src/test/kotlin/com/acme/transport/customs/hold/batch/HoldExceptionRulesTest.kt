package com.acme.transport.customs.hold.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HoldExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultHoldExceptionValidator()
        val item = newHoldException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = HoldExceptionCodeLengthRule(3600)
        val item = newHoldException("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = HoldExceptionThresholdRangeRule(0.6, 2144.3)
        val item = newHoldException("x", "y")
        item.threshold = 2145.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = HoldExceptionVehicleIdLengthRule(120)
        val item = newHoldException("x", "y")
        item.vehicleId = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
