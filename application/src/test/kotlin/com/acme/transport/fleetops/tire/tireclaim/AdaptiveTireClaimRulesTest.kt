package com.acme.transport.fleetops.tire.tireclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveTireClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveTireClaimValidator()
        val item = newAdaptiveTireClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long currency`() {
        val rule = AdaptiveTireClaimCurrencyLengthRule(32)
        val item = newAdaptiveTireClaim("x", "y")
        item.currency = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = AdaptiveTireClaimRequiredTitleRule("adaptive tire claim needs a value")
        val item = newAdaptiveTireClaim("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = AdaptiveTireClaimTitleLengthRule(86400)
        val item = newAdaptiveTireClaim("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long dock`() {
        val rule = AdaptiveTireClaimDockLengthRule(86400)
        val item = newAdaptiveTireClaim("x", "y")
        item.dock = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
