package com.acme.transport.safety.cameraevent.eventslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackCameraEventSnapshotTest {
    @Test
    fun `starts in proposed`() {
        val item = newFallbackCameraEventSnapshot("code-134", "label-938")
        assertEquals(FallbackCameraEventSnapshotStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newFallbackCameraEventSnapshot("code-134", "label-938")
        try {
            item.moveTo(FallbackCameraEventSnapshotStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackCameraEventSnapshotStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newFallbackCameraEventSnapshot("code-134", "label-938")
        item.status = FallbackCameraEventSnapshotStatus.WITHDRAWN
        assertFails("move") { item.moveTo(FallbackCameraEventSnapshotStatus.PROPOSED) }
    }
}
