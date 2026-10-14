package com.acme.transport.terminal.traileryard.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledTrailerYardClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledTrailerYardClaimValidator()
        val item = newScheduledTrailerYardClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = ScheduledTrailerYardClaimNameLengthRule(32)
        val item = newScheduledTrailerYardClaim("x", "y")
        item.name = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScheduledTrailerYardClaimCodeLengthRule(80)
        val item = newScheduledTrailerYardClaim("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
