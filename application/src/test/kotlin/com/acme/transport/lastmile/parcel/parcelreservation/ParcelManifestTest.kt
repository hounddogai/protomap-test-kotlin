package com.acme.transport.lastmile.parcel.parcelreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ParcelManifestTest {
    @Test
    fun `starts in idle`() {
        val item = newParcelManifest("code-95", "label-844")
        assertEquals(ParcelManifestStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newParcelManifest("code-95", "label-844")
        try {
            item.moveTo(ParcelManifestStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ParcelManifestStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newParcelManifest("code-95", "label-844")
        item.status = ParcelManifestStatus.STOPPED
        assertFails("move") { item.moveTo(ParcelManifestStatus.IDLE) }
    }
}
