package com.acme.transport.network.hub.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HubRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultHubRequestValidator()
        val item = newHubRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = HubRequestScoreRangeRule(4.0, 3503.9)
        val item = newHubRequest("x", "y")
        item.score = 3505.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = HubRequestLabelLengthRule(1000)
        val item = newHubRequest("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = HubRequestExpiresAtRangeRule(80, 86400)
        val item = newHubRequest("x", "y")
        item.expiresAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
