package com.acme.transport.network.hub.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HubBaselineTest {
    @Test
    fun `starts in planned`() {
        val item = newHubBaseline("code-758", "title-761")
        assertEquals(HubBaselineStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newHubBaseline("code-758", "title-761")
        try {
            item.moveTo(HubBaselineStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HubBaselineStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newHubBaseline("code-758", "title-761")
        item.status = HubBaselineStatus.CANCELLED
        assertFails("move") { item.moveTo(HubBaselineStatus.PLANNED) }
    }
}
