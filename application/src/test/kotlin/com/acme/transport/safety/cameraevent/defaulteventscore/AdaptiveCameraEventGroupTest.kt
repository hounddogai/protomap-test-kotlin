package com.acme.transport.safety.cameraevent.defaulteventscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveCameraEventGroupTest {
    @Test
    fun `starts in new`() {
        val item = newAdaptiveCameraEventGroup("code-849", "title-775")
        assertEquals(AdaptiveCameraEventGroupStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newAdaptiveCameraEventGroup("code-849", "title-775")
        try {
            item.moveTo(AdaptiveCameraEventGroupStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveCameraEventGroupStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newAdaptiveCameraEventGroup("code-849", "title-775")
        item.status = AdaptiveCameraEventGroupStatus.FAILED
        assertFails("move") { item.moveTo(AdaptiveCameraEventGroupStatus.NEW) }
    }
}
