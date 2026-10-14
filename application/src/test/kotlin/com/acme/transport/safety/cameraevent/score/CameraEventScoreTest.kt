package com.acme.transport.safety.cameraevent.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CameraEventScoreTest {
    @Test
    fun `starts in planned`() {
        val item = newCameraEventScore("code-754", "name-539")
        assertEquals(CameraEventScoreStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newCameraEventScore("code-754", "name-539")
        try {
            item.moveTo(CameraEventScoreStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CameraEventScoreStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newCameraEventScore("code-754", "name-539")
        item.status = CameraEventScoreStatus.CANCELLED
        assertFails("move") { item.moveTo(CameraEventScoreStatus.PLANNED) }
    }
}
