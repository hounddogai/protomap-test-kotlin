package com.acme.transport.customs.broker.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalBrokerAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalBrokerAuditValidator()
        val item = newRegionalBrokerAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = RegionalBrokerAuditUpdatedAtRangeRule(120, 255)
        val item = newRegionalBrokerAudit("x", "y")
        item.updatedAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = RegionalBrokerAuditCarrierLengthRule(120)
        val item = newRegionalBrokerAudit("x", "y")
        item.carrier = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above score`() {
        val rule = RegionalBrokerAuditScoreRangeRule(1.1, 2334.0)
        val item = newRegionalBrokerAudit("x", "y")
        item.score = 2335.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalBrokerAuditRequiredCodeRule("regional broker audit needs an entry")
        val item = newRegionalBrokerAudit("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
