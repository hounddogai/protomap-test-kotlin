package com.acme.transport.safety.cameraevent.evententry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryCameraEventDigestTest {
    @Test
    fun `starts in new`() {
        val item = newPrimaryCameraEventDigest("code-912", "title-16")
        assertEquals(PrimaryCameraEventDigestStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newPrimaryCameraEventDigest("code-912", "title-16")
        try {
            item.moveTo(PrimaryCameraEventDigestStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryCameraEventDigestStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newPrimaryCameraEventDigest("code-912", "title-16")
        item.status = PrimaryCameraEventDigestStatus.FAILED
        assertFails("move") { item.moveTo(PrimaryCameraEventDigestStatus.NEW) }
    }
}
