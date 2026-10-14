package com.acme.transport.safety.cameraevent.audit

import kotlin.test.Test
import kotlin.test.assertEquals

class CameraEventQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCameraEventQuota("code-75", "name-689")
        val view = toCameraEventQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCameraEventQuotaViews(mutableListOf<CameraEventQuota>(item, item)).size, "views")
    }
}
