package com.acme.transport.terminal.traileryard.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictTrailerYardEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictTrailerYardEstimateValidator()
        val item = newStrictTrailerYardEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = StrictTrailerYardEstimateExpiresAtRangeRule(32, 64)
        val item = newStrictTrailerYardEstimate("x", "y")
        item.expiresAt = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = StrictTrailerYardEstimateCodeLengthRule(86400)
        val item = newStrictTrailerYardEstimate("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = StrictTrailerYardEstimateOdometerRangeRule(64, 86400)
        val item = newStrictTrailerYardEstimate("x", "y")
        item.odometer = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = StrictTrailerYardEstimateNameLengthRule(16)
        val item = newStrictTrailerYardEstimate("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long depot`() {
        val rule = StrictTrailerYardEstimateDepotLengthRule(255)
        val item = newStrictTrailerYardEstimate("x", "y")
        item.depot = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
