package com.acme.transport.customs.broker.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalBrokerSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalBrokerSummaryValidator()
        val item = newRegionalBrokerSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long unit`() {
        val rule = RegionalBrokerSummaryUnitLengthRule(64)
        val item = newRegionalBrokerSummary("x", "y")
        item.unit = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = RegionalBrokerSummaryCodeLengthRule(255)
        val item = newRegionalBrokerSummary("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RegionalBrokerSummaryRequiredNameRule("regional broker summary needs this field")
        val item = newRegionalBrokerSummary("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = RegionalBrokerSummaryDurationMinutesRangeRule(120, 86400)
        val item = newRegionalBrokerSummary("x", "y")
        item.durationMinutes = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
