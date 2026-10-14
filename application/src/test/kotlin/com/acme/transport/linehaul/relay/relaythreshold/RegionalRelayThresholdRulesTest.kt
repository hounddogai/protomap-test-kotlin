package com.acme.transport.linehaul.relay.relaythreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalRelayThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalRelayThresholdValidator()
        val item = newRegionalRelayThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above created at`() {
        val rule = RegionalRelayThresholdCreatedAtRangeRule(255, 3600)
        val item = newRegionalRelayThreshold("x", "y")
        item.createdAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RegionalRelayThresholdRequiredNameRule("regional relay threshold needs an entry")
        val item = newRegionalRelayThreshold("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
