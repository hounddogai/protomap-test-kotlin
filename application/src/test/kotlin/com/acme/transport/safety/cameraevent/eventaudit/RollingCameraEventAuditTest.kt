package com.acme.transport.safety.cameraevent.eventaudit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingCameraEventAuditTest {
    @Test
    fun `starts in idle`() {
        val item = newRollingCameraEventAudit("code-800", "label-686")
        assertEquals(RollingCameraEventAuditStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newRollingCameraEventAudit("code-800", "label-686")
        try {
            item.moveTo(RollingCameraEventAuditStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingCameraEventAuditStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRollingCameraEventAudit("code-800", "label-686")
        item.status = RollingCameraEventAuditStatus.STOPPED
        assertFails("move") { item.moveTo(RollingCameraEventAuditStatus.IDLE) }
    }
}
