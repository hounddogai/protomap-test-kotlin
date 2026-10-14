package com.acme.transport.safety.cameraevent.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultCameraEventEntryTest {
    @Test
    fun `starts in new`() {
        val item = newDefaultCameraEventEntry("code-485", "label-530")
        assertEquals(DefaultCameraEventEntryStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newDefaultCameraEventEntry("code-485", "label-530")
        try {
            item.moveTo(DefaultCameraEventEntryStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultCameraEventEntryStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newDefaultCameraEventEntry("code-485", "label-530")
        item.status = DefaultCameraEventEntryStatus.FAILED
        assertFails("move") { item.moveTo(DefaultCameraEventEntryStatus.NEW) }
    }
}
