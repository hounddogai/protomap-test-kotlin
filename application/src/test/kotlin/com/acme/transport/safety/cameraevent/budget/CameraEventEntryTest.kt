package com.acme.transport.safety.cameraevent.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CameraEventEntryTest {
    @Test
    fun `starts in created`() {
        val item = newCameraEventEntry("code-507", "name-84")
        assertEquals(CameraEventEntryStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newCameraEventEntry("code-507", "name-84")
        try {
            item.moveTo(CameraEventEntryStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CameraEventEntryStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newCameraEventEntry("code-507", "name-84")
        item.status = CameraEventEntryStatus.RETIRED
        assertFails("move") { item.moveTo(CameraEventEntryStatus.CREATED) }
    }
}
