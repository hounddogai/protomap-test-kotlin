package com.acme.transport.safety.cameraevent.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CameraEventTierTest {
    @Test
    fun `starts in new`() {
        val item = newCameraEventTier("code-612", "title-810")
        assertEquals(CameraEventTierStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newCameraEventTier("code-612", "title-810")
        try {
            item.moveTo(CameraEventTierStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CameraEventTierStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newCameraEventTier("code-612", "title-810")
        item.status = CameraEventTierStatus.FAILED
        assertFails("move") { item.moveTo(CameraEventTierStatus.NEW) }
    }
}
