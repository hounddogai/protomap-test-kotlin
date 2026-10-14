package com.acme.transport.network.hub.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HubAuditTest {
    @Test
    fun `starts in planned`() {
        val item = newHubAudit("code-137", "name-587")
        assertEquals(HubAuditStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newHubAudit("code-137", "name-587")
        try {
            item.moveTo(HubAuditStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HubAuditStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newHubAudit("code-137", "name-587")
        item.status = HubAuditStatus.CANCELLED
        assertFails("move") { item.moveTo(HubAuditStatus.PLANNED) }
    }
}
