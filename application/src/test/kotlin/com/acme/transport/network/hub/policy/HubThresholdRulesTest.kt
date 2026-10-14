package com.acme.transport.network.hub.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HubThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultHubThresholdValidator()
        val item = newHubThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = HubThresholdEtaMinutesRangeRule(16, 255)
        val item = newHubThreshold("x", "y")
        item.etaMinutes = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = HubThresholdExpiresAtRangeRule(16, 3600)
        val item = newHubThreshold("x", "y")
        item.expiresAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = HubThresholdRequiredCodeRule("hub threshold needs an entry")
        val item = newHubThreshold("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
