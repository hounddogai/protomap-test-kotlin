package com.acme.transport.safety.cameraevent.eventclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualCameraEventBaselineTest {
    @Test
    fun `starts in idle`() {
        val item = newManualCameraEventBaseline("code-111", "name-458")
        assertEquals(ManualCameraEventBaselineStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newManualCameraEventBaseline("code-111", "name-458")
        try {
            item.moveTo(ManualCameraEventBaselineStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualCameraEventBaselineStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newManualCameraEventBaseline("code-111", "name-458")
        item.status = ManualCameraEventBaselineStatus.STOPPED
        assertFails("move") { item.moveTo(ManualCameraEventBaselineStatus.IDLE) }
    }
}
