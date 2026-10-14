package com.acme.transport.linehaul.tender.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientTenderCheckpointTest {
    @Test
    fun `starts in idle`() {
        val item = newLenientTenderCheckpoint("code-432", "label-303")
        assertEquals(LenientTenderCheckpointStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newLenientTenderCheckpoint("code-432", "label-303")
        try {
            item.moveTo(LenientTenderCheckpointStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientTenderCheckpointStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLenientTenderCheckpoint("code-432", "label-303")
        item.status = LenientTenderCheckpointStatus.STOPPED
        assertFails("move") { item.moveTo(LenientTenderCheckpointStatus.IDLE) }
    }
}
