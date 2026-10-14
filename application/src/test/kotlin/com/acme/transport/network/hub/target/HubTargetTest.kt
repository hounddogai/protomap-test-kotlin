package com.acme.transport.network.hub.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HubTargetTest {
    @Test
    fun `starts in draft`() {
        val item = newHubTarget("code-63", "title-550")
        assertEquals(HubTargetStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newHubTarget("code-63", "title-550")
        try {
            item.moveTo(HubTargetStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HubTargetStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newHubTarget("code-63", "title-550")
        item.status = HubTargetStatus.CLOSED
        assertFails("move") { item.moveTo(HubTargetStatus.DRAFT) }
    }
}
