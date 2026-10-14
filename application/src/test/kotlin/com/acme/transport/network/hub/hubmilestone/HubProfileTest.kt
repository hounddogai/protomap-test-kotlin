package com.acme.transport.network.hub.hubmilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HubProfileTest {
    @Test
    fun `starts in requested`() {
        val item = newHubProfile("code-24", "label-53")
        assertEquals(HubProfileStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newHubProfile("code-24", "label-53")
        try {
            item.moveTo(HubProfileStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HubProfileStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newHubProfile("code-24", "label-53")
        item.status = HubProfileStatus.EXPIRED
        assertFails("move") { item.moveTo(HubProfileStatus.REQUESTED) }
    }
}
