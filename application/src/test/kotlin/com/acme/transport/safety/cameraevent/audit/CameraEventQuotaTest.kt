package com.acme.transport.safety.cameraevent.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CameraEventQuotaTest {
    @Test
    fun `starts in draft`() {
        val item = newCameraEventQuota("code-75", "name-689")
        assertEquals(CameraEventQuotaStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newCameraEventQuota("code-75", "name-689")
        try {
            item.moveTo(CameraEventQuotaStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CameraEventQuotaStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newCameraEventQuota("code-75", "name-689")
        item.status = CameraEventQuotaStatus.CLOSED
        assertFails("move") { item.moveTo(CameraEventQuotaStatus.DRAFT) }
    }
}
