package com.acme.transport.safety.cameraevent.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredCameraEventTargetTest {
    @Test
    fun `starts in new`() {
        val item = newTieredCameraEventTarget("code-646", "label-664")
        assertEquals(TieredCameraEventTargetStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newTieredCameraEventTarget("code-646", "label-664")
        try {
            item.moveTo(TieredCameraEventTargetStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredCameraEventTargetStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newTieredCameraEventTarget("code-646", "label-664")
        item.status = TieredCameraEventTargetStatus.FAILED
        assertFails("move") { item.moveTo(TieredCameraEventTargetStatus.NEW) }
    }
}
