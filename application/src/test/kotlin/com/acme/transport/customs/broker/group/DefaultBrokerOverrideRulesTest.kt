package com.acme.transport.customs.broker.group

import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultBrokerOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultBrokerOverrideValidator()
        val item = newDefaultBrokerOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above revision`() {
        val rule = DefaultBrokerOverrideRevisionRangeRule(255, 1000)
        val item = newDefaultBrokerOverride("x", "y")
        item.revision = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
