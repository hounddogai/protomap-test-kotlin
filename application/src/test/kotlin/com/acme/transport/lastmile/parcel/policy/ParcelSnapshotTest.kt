package com.acme.transport.lastmile.parcel.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ParcelSnapshotTest {
    @Test
    fun `starts in requested`() {
        val item = newParcelSnapshot("code-3", "label-120")
        assertEquals(ParcelSnapshotStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newParcelSnapshot("code-3", "label-120")
        try {
            item.moveTo(ParcelSnapshotStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ParcelSnapshotStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newParcelSnapshot("code-3", "label-120")
        item.status = ParcelSnapshotStatus.EXPIRED
        assertFails("move") { item.moveTo(ParcelSnapshotStatus.REQUESTED) }
    }
}
