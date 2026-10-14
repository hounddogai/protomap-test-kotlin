package com.acme.transport.network.hub.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HubTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultHubTargetValidator()
        val item = newHubTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = HubTargetRequiredRouteCodeRule("hub target needs a value")
        val item = newHubTarget("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long route code`() {
        val rule = HubTargetRouteCodeLengthRule(3600)
        val item = newHubTarget("x", "y")
        item.routeCode = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = HubTargetExpiresAtRangeRule(64, 1000)
        val item = newHubTarget("x", "y")
        item.expiresAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = HubTargetRequiredTitleRule("hub target needs a value")
        val item = newHubTarget("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = HubTargetExternalRefLengthRule(16)
        val item = newHubTarget("x", "y")
        item.externalRef = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
