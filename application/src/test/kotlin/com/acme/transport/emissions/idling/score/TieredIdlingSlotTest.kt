package com.acme.transport.emissions.idling.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredIdlingSlotTest {
    @Test
    fun `starts in draft`() {
        val item = newTieredIdlingSlot("code-918", "title-584")
        assertEquals(TieredIdlingSlotStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newTieredIdlingSlot("code-918", "title-584")
        try {
            item.moveTo(TieredIdlingSlotStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredIdlingSlotStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newTieredIdlingSlot("code-918", "title-584")
        item.status = TieredIdlingSlotStatus.CLOSED
        assertFails("move") { item.moveTo(TieredIdlingSlotStatus.DRAFT) }
    }
}
