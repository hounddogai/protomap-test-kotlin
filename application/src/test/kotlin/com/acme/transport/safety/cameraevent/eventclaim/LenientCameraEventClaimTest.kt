package com.acme.transport.safety.cameraevent.eventclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientCameraEventClaimTest {
    @Test
    fun `starts in new`() {
        val item = newLenientCameraEventClaim("code-670", "title-558")
        assertEquals(LenientCameraEventClaimStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newLenientCameraEventClaim("code-670", "title-558")
        try {
            item.moveTo(LenientCameraEventClaimStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientCameraEventClaimStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newLenientCameraEventClaim("code-670", "title-558")
        item.status = LenientCameraEventClaimStatus.FAILED
        assertFails("move") { item.moveTo(LenientCameraEventClaimStatus.NEW) }
    }
}
