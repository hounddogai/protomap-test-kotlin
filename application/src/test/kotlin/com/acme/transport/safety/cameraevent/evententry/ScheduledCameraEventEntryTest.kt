package com.acme.transport.safety.cameraevent.evententry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledCameraEventEntryTest {
    @Test
    fun `starts in planned`() {
        val item = newScheduledCameraEventEntry("code-128", "title-181")
        assertEquals(ScheduledCameraEventEntryStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newScheduledCameraEventEntry("code-128", "title-181")
        try {
            item.moveTo(ScheduledCameraEventEntryStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledCameraEventEntryStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newScheduledCameraEventEntry("code-128", "title-181")
        item.status = ScheduledCameraEventEntryStatus.CANCELLED
        assertFails("move") { item.moveTo(ScheduledCameraEventEntryStatus.PLANNED) }
    }
}
