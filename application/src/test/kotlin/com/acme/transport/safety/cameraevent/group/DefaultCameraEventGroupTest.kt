package com.acme.transport.safety.cameraevent.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultCameraEventGroupTest {
    @Test
    fun `starts in requested`() {
        val item = newDefaultCameraEventGroup("code-193", "label-833")
        assertEquals(DefaultCameraEventGroupStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newDefaultCameraEventGroup("code-193", "label-833")
        try {
            item.moveTo(DefaultCameraEventGroupStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultCameraEventGroupStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newDefaultCameraEventGroup("code-193", "label-833")
        item.status = DefaultCameraEventGroupStatus.EXPIRED
        assertFails("move") { item.moveTo(DefaultCameraEventGroupStatus.REQUESTED) }
    }
}
