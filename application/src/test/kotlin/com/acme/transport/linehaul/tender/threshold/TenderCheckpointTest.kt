package com.acme.transport.linehaul.tender.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TenderCheckpointTest {
    @Test
    fun `starts in draft`() {
        val item = newTenderCheckpoint("code-733", "title-595")
        assertEquals(TenderCheckpointStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newTenderCheckpoint("code-733", "title-595")
        try {
            item.moveTo(TenderCheckpointStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TenderCheckpointStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newTenderCheckpoint("code-733", "title-595")
        item.status = TenderCheckpointStatus.CLOSED
        assertFails("move") { item.moveTo(TenderCheckpointStatus.DRAFT) }
    }
}
