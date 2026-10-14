package com.acme.transport.linehaul.tender.tenderestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackTenderSlotTest {
    @Test
    fun `starts in new`() {
        val item = newFallbackTenderSlot("code-855", "title-658")
        assertEquals(FallbackTenderSlotStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newFallbackTenderSlot("code-855", "title-658")
        try {
            item.moveTo(FallbackTenderSlotStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackTenderSlotStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newFallbackTenderSlot("code-855", "title-658")
        item.status = FallbackTenderSlotStatus.FAILED
        assertFails("move") { item.moveTo(FallbackTenderSlotStatus.NEW) }
    }
}
