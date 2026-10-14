package com.acme.transport.safety.cameraevent.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingCameraEventGroupTest {
    @Test
    fun `starts in idle`() {
        val item = newRollingCameraEventGroup("code-811", "name-650")
        assertEquals(RollingCameraEventGroupStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newRollingCameraEventGroup("code-811", "name-650")
        try {
            item.moveTo(RollingCameraEventGroupStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingCameraEventGroupStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRollingCameraEventGroup("code-811", "name-650")
        item.status = RollingCameraEventGroupStatus.STOPPED
        assertFails("move") { item.moveTo(RollingCameraEventGroupStatus.IDLE) }
    }
}
