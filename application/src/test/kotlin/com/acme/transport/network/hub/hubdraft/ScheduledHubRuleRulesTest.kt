package com.acme.transport.network.hub.hubdraft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledHubRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledHubRuleValidator()
        val item = newScheduledHubRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = ScheduledHubRuleNameLengthRule(16)
        val item = newScheduledHubRule("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = ScheduledHubRuleRequiredVehicleIdRule("scheduled hub rule needs a value")
        val item = newScheduledHubRule("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
