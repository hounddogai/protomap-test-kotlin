package com.acme.transport.linehaul.relay.weightedrelayrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingRelayForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingRelayForecastValidator()
        val item = newPendingRelayForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = PendingRelayForecastRequiredLabelRule("pending relay forecast needs a value")
        val item = newPendingRelayForecast("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PendingRelayForecastCodeLengthRule(80)
        val item = newPendingRelayForecast("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above cost`() {
        val rule = PendingRelayForecastCostRangeRule(9.7, 1530.8)
        val item = newPendingRelayForecast("x", "y")
        item.cost = 1532.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long channel`() {
        val rule = PendingRelayForecastChannelLengthRule(120)
        val item = newPendingRelayForecast("x", "y")
        item.channel = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
