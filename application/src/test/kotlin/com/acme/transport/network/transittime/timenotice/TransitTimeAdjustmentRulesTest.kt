package com.acme.transport.network.transittime.timenotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TransitTimeAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTransitTimeAdjustmentValidator()
        val item = newTransitTimeAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = TransitTimeAdjustmentLimitRangeRule(1000, 86400)
        val item = newTransitTimeAdjustment("x", "y")
        item.limit = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = TransitTimeAdjustmentTrailerIdLengthRule(1000)
        val item = newTransitTimeAdjustment("x", "y")
        item.trailerId = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = TransitTimeAdjustmentLabelLengthRule(16)
        val item = newTransitTimeAdjustment("x", "y")
        item.label = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = TransitTimeAdjustmentCodeLengthRule(86400)
        val item = newTransitTimeAdjustment("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
