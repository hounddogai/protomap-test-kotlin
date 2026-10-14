package com.acme.transport.safety.cameraevent.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CameraEventAuditTest {
    @Test
    fun `starts in open`() {
        val item = newCameraEventAudit("code-588", "label-182")
        assertEquals(CameraEventAuditStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newCameraEventAudit("code-588", "label-182")
        try {
            item.moveTo(CameraEventAuditStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CameraEventAuditStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newCameraEventAudit("code-588", "label-182")
        item.status = CameraEventAuditStatus.ARCHIVED
        assertFails("move") { item.moveTo(CameraEventAuditStatus.OPEN) }
    }
}
