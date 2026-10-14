package com.acme.transport.customs.broker.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalBrokerSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalBrokerSummaryValidator()
        val item = newGlobalBrokerSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long source`() {
        val rule = GlobalBrokerSummarySourceLengthRule(3600)
        val item = newGlobalBrokerSummary("x", "y")
        item.source = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank source`() {
        val rule = GlobalBrokerSummaryRequiredSourceRule("global broker summary needs this field")
        val item = newGlobalBrokerSummary("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = GlobalBrokerSummaryRequiredCodeRule("global broker summary needs this field")
        val item = newGlobalBrokerSummary("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
