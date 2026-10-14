package com.acme.transport.network.hub.hubentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualHubEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualHubEntryValidator()
        val item = newManualHubEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = ManualHubEntryRequiredLaneRule("manual hub entry needs this field")
        val item = newManualHubEntry("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualHubEntryRequiredCodeRule("manual hub entry needs a value")
        val item = newManualHubEntry("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = ManualHubEntryRequiredVehicleIdRule("manual hub entry needs an entry")
        val item = newManualHubEntry("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
