package com.acme.transport.lastmile.parcel.parcelslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledParcelDigestTest {
    @Test
    fun `starts in new`() {
        val item = newScheduledParcelDigest("code-931", "label-861")
        assertEquals(ScheduledParcelDigestStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newScheduledParcelDigest("code-931", "label-861")
        try {
            item.moveTo(ScheduledParcelDigestStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledParcelDigestStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newScheduledParcelDigest("code-931", "label-861")
        item.status = ScheduledParcelDigestStatus.FAILED
        assertFails("move") { item.moveTo(ScheduledParcelDigestStatus.NEW) }
    }
}
