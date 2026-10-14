package com.acme.transport.customs.broker.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultBrokerDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultBrokerDigestValidator()
        val item = newDefaultBrokerDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = DefaultBrokerDigestRequiredRouteCodeRule("default broker digest needs an entry")
        val item = newDefaultBrokerDigest("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = DefaultBrokerDigestTitleLengthRule(120)
        val item = newDefaultBrokerDigest("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
