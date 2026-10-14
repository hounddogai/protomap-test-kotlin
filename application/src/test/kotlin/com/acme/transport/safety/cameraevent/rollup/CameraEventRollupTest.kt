package com.acme.transport.safety.cameraevent.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CameraEventRollupTest {
    @Test
    fun `starts in planned`() {
        val item = newCameraEventRollup("code-145", "label-952")
        assertEquals(CameraEventRollupStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newCameraEventRollup("code-145", "label-952")
        try {
            item.moveTo(CameraEventRollupStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CameraEventRollupStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newCameraEventRollup("code-145", "label-952")
        item.status = CameraEventRollupStatus.CANCELLED
        assertFails("move") { item.moveTo(CameraEventRollupStatus.PLANNED) }
    }
}
