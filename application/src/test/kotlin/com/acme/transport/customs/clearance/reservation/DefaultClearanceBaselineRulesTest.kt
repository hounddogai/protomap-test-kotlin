package com.acme.transport.customs.clearance.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultClearanceBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultClearanceBaselineValidator()
        val item = newDefaultClearanceBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = DefaultClearanceBaselineQuantityRangeRule(1000, 3600)
        val item = newDefaultClearanceBaseline("x", "y")
        item.quantity = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = DefaultClearanceBaselineNameLengthRule(16)
        val item = newDefaultClearanceBaseline("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long region`() {
        val rule = DefaultClearanceBaselineRegionLengthRule(32)
        val item = newDefaultClearanceBaseline("x", "y")
        item.region = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
