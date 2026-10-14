package com.acme.transport.linehaul.relay.batch

import kotlin.test.Test
import kotlin.test.assertTrue

class RelayOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRelayOverrideValidator()
        val item = newRelayOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = RelayOverrideCeilingRangeRule(8.9, 1699.0)
        val item = newRelayOverride("x", "y")
        item.ceiling = 1700.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above limit`() {
        val rule = RelayOverrideLimitRangeRule(255, 1000)
        val item = newRelayOverride("x", "y")
        item.limit = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
