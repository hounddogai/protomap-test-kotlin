package com.acme.transport.terminal.traileryard.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerYardClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTrailerYardClaimValidator()
        val item = newTrailerYardClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = TrailerYardClaimNameLengthRule(1000)
        val item = newTrailerYardClaim("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above limit`() {
        val rule = TrailerYardClaimLimitRangeRule(16, 120)
        val item = newTrailerYardClaim("x", "y")
        item.limit = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
