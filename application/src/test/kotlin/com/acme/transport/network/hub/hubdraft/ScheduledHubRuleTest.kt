package com.acme.transport.network.hub.hubdraft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledHubRuleTest {
    @Test
    fun `starts in idle`() {
        val item = newScheduledHubRule("code-624", "name-275")
        assertEquals(ScheduledHubRuleStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newScheduledHubRule("code-624", "name-275")
        try {
            item.moveTo(ScheduledHubRuleStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledHubRuleStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newScheduledHubRule("code-624", "name-275")
        item.status = ScheduledHubRuleStatus.STOPPED
        assertFails("move") { item.moveTo(ScheduledHubRuleStatus.IDLE) }
    }
}
