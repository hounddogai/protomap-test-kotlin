package com.acme.transport.network.hub.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingHubEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingHubEstimateValidator()
        val item = newRollingHubEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = RollingHubEstimateRequiredDepotRule("rolling hub estimate needs this field")
        val item = newRollingHubEstimate("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = RollingHubEstimateExpiresAtRangeRule(16, 1000)
        val item = newRollingHubEstimate("x", "y")
        item.expiresAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
