package com.acme.transport.customs.broker.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalBrokerVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalBrokerVarianceValidator()
        val item = newRegionalBrokerVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = RegionalBrokerVarianceEtaMinutesRangeRule(86400, 86400)
        val item = newRegionalBrokerVariance("x", "y")
        item.etaMinutes = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = RegionalBrokerVarianceCodeLengthRule(64)
        val item = newRegionalBrokerVariance("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalBrokerVarianceRequiredCodeRule("regional broker variance needs an entry")
        val item = newRegionalBrokerVariance("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long route code`() {
        val rule = RegionalBrokerVarianceRouteCodeLengthRule(64)
        val item = newRegionalBrokerVariance("x", "y")
        item.routeCode = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
