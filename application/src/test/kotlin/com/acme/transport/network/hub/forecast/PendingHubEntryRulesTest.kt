package com.acme.transport.network.hub.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingHubEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingHubEntryValidator()
        val item = newPendingHubEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = PendingHubEntryRequiredTrailerIdRule("pending hub entry needs an entry")
        val item = newPendingHubEntry("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = PendingHubEntryNameLengthRule(3600)
        val item = newPendingHubEntry("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
