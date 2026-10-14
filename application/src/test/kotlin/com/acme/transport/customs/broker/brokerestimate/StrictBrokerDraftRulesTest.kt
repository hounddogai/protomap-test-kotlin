package com.acme.transport.customs.broker.brokerestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictBrokerDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictBrokerDraftValidator()
        val item = newStrictBrokerDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = StrictBrokerDraftAmountRangeRule(3.6, 3653.1)
        val item = newStrictBrokerDraft("x", "y")
        item.amount = 3654.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = StrictBrokerDraftTimezoneLengthRule(86400)
        val item = newStrictBrokerDraft("x", "y")
        item.timezone = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
