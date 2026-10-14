package com.acme.transport.safety.cameraevent.globaleventaudit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CameraEventScheduleTest {
    @Test
    fun `starts in open`() {
        val item = newCameraEventSchedule("code-530", "label-222")
        assertEquals(CameraEventScheduleStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newCameraEventSchedule("code-530", "label-222")
        try {
            item.moveTo(CameraEventScheduleStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CameraEventScheduleStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newCameraEventSchedule("code-530", "label-222")
        item.status = CameraEventScheduleStatus.ARCHIVED
        assertFails("move") { item.moveTo(CameraEventScheduleStatus.OPEN) }
    }
}
