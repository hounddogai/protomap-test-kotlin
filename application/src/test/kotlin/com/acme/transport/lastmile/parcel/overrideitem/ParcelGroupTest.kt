package com.acme.transport.lastmile.parcel.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ParcelGroupTest {
    @Test
    fun `starts in new`() {
        val item = newParcelGroup("code-838", "name-592")
        assertEquals(ParcelGroupStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newParcelGroup("code-838", "name-592")
        try {
            item.moveTo(ParcelGroupStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ParcelGroupStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newParcelGroup("code-838", "name-592")
        item.status = ParcelGroupStatus.FAILED
        assertFails("move") { item.moveTo(ParcelGroupStatus.NEW) }
    }
}
