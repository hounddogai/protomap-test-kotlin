package com.acme.transport.safety.cameraevent.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CameraEventAssignmentTest {
    @Test
    fun `starts in created`() {
        val item = newCameraEventAssignment("code-100", "label-629")
        assertEquals(CameraEventAssignmentStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newCameraEventAssignment("code-100", "label-629")
        try {
            item.moveTo(CameraEventAssignmentStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CameraEventAssignmentStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newCameraEventAssignment("code-100", "label-629")
        item.status = CameraEventAssignmentStatus.RETIRED
        assertFails("move") { item.moveTo(CameraEventAssignmentStatus.CREATED) }
    }
}
