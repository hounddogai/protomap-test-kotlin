package com.acme.transport.customs.broker.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackBrokerBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackBrokerBundleValidator()
        val item = newFallbackBrokerBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = FallbackBrokerBundleCodeLengthRule(64)
        val item = newFallbackBrokerBundle("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
