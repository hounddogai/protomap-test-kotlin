package com.acme.transport.linehaul.tender.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TenderSnapshotTest {
    @Test
    fun `starts in idle`() {
        val item = newTenderSnapshot("code-215", "label-503")
        assertEquals(TenderSnapshotStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newTenderSnapshot("code-215", "label-503")
        try {
            item.moveTo(TenderSnapshotStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TenderSnapshotStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newTenderSnapshot("code-215", "label-503")
        item.status = TenderSnapshotStatus.STOPPED
        assertFails("move") { item.moveTo(TenderSnapshotStatus.IDLE) }
    }
}
