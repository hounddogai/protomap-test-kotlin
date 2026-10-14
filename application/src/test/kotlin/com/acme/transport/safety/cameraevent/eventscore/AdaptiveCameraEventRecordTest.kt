package com.acme.transport.safety.cameraevent.eventscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveCameraEventRecordTest {
    @Test
    fun `starts in new`() {
        val item = newAdaptiveCameraEventRecord("code-481", "title-945")
        assertEquals(AdaptiveCameraEventRecordStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newAdaptiveCameraEventRecord("code-481", "title-945")
        try {
            item.moveTo(AdaptiveCameraEventRecordStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveCameraEventRecordStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newAdaptiveCameraEventRecord("code-481", "title-945")
        item.status = AdaptiveCameraEventRecordStatus.FAILED
        assertFails("move") { item.moveTo(AdaptiveCameraEventRecordStatus.NEW) }
    }
}
