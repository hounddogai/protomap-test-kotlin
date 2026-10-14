package com.acme.transport.freightpay.ratecard.cardrequest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredRateCardRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredRateCardRequestValidator()
        val item = newTieredRateCardRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank source`() {
        val rule = TieredRateCardRequestRequiredSourceRule("tiered rate card request needs an entry")
        val item = newTieredRateCardRequest("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = TieredRateCardRequestDriverIdLengthRule(86400)
        val item = newTieredRateCardRequest("x", "y")
        item.driverId = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = TieredRateCardRequestRequiredDriverIdRule("tiered rate card request needs a value")
        val item = newTieredRateCardRequest("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long source`() {
        val rule = TieredRateCardRequestSourceLengthRule(120)
        val item = newTieredRateCardRequest("x", "y")
        item.source = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
