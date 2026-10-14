package com.acme.transport.lastmile.parcel.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ParcelEntryTest {
    @Test
    fun `starts in draft`() {
        val item = newParcelEntry("code-694", "label-919")
        assertEquals(ParcelEntryStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newParcelEntry("code-694", "label-919")
        try {
            item.moveTo(ParcelEntryStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ParcelEntryStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newParcelEntry("code-694", "label-919")
        item.status = ParcelEntryStatus.CLOSED
        assertFails("move") { item.moveTo(ParcelEntryStatus.DRAFT) }
    }
}
