package com.acme.transport.safety.cameraevent.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingCameraEventSnapshotTest {
    @Test
    fun `starts in proposed`() {
        val item = newRollingCameraEventSnapshot("code-188", "title-641")
        assertEquals(RollingCameraEventSnapshotStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newRollingCameraEventSnapshot("code-188", "title-641")
        try {
            item.moveTo(RollingCameraEventSnapshotStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingCameraEventSnapshotStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRollingCameraEventSnapshot("code-188", "title-641")
        item.status = RollingCameraEventSnapshotStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RollingCameraEventSnapshotStatus.PROPOSED) }
    }
}
