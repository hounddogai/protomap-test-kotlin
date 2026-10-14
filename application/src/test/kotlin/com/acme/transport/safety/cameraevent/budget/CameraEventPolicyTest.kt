package com.acme.transport.safety.cameraevent.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CameraEventPolicyTest {
    @Test
    fun `starts in planned`() {
        val item = newCameraEventPolicy("code-236", "name-900")
        assertEquals(CameraEventPolicyStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newCameraEventPolicy("code-236", "name-900")
        try {
            item.moveTo(CameraEventPolicyStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CameraEventPolicyStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newCameraEventPolicy("code-236", "name-900")
        item.status = CameraEventPolicyStatus.CANCELLED
        assertFails("move") { item.moveTo(CameraEventPolicyStatus.PLANNED) }
    }
}
