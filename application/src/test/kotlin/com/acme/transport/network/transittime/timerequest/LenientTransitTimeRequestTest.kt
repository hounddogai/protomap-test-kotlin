package com.acme.transport.network.transittime.timerequest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientTransitTimeRequestTest {
    @Test
    fun `starts in new`() {
        val item = newLenientTransitTimeRequest("code-604", "title-272")
        assertEquals(LenientTransitTimeRequestStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newLenientTransitTimeRequest("code-604", "title-272")
        try {
            item.moveTo(LenientTransitTimeRequestStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientTransitTimeRequestStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newLenientTransitTimeRequest("code-604", "title-272")
        item.status = LenientTransitTimeRequestStatus.FAILED
        assertFails("move") { item.moveTo(LenientTransitTimeRequestStatus.NEW) }
    }
}
