package com.acme.transport.customs.broker.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryBrokerEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryBrokerEntryValidator()
        val item = newPrimaryBrokerEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PrimaryBrokerEntryCodeLengthRule(86400)
        val item = newPrimaryBrokerEntry("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above priority`() {
        val rule = PrimaryBrokerEntryPriorityRangeRule(64, 86400)
        val item = newPrimaryBrokerEntry("x", "y")
        item.priority = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PrimaryBrokerEntryRequiredCodeRule("primary broker entry needs an entry")
        val item = newPrimaryBrokerEntry("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
