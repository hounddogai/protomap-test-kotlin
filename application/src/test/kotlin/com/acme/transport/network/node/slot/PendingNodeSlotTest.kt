package com.acme.transport.network.node.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingNodeSlotTest {
    @Test
    fun `starts in created`() {
        val item = newPendingNodeSlot("code-115", "title-425")
        assertEquals(PendingNodeSlotStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newPendingNodeSlot("code-115", "title-425")
        try {
            item.moveTo(PendingNodeSlotStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingNodeSlotStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newPendingNodeSlot("code-115", "title-425")
        item.status = PendingNodeSlotStatus.RETIRED
        assertFails("move") { item.moveTo(PendingNodeSlotStatus.CREATED) }
    }
}
