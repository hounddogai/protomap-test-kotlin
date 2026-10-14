package com.acme.transport.safety.cameraevent.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CameraEventClaimTest {
    @Test
    fun `starts in new`() {
        val item = newCameraEventClaim("code-266", "label-952")
        assertEquals(CameraEventClaimStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newCameraEventClaim("code-266", "label-952")
        try {
            item.moveTo(CameraEventClaimStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CameraEventClaimStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newCameraEventClaim("code-266", "label-952")
        item.status = CameraEventClaimStatus.FAILED
        assertFails("move") { item.moveTo(CameraEventClaimStatus.NEW) }
    }
}
