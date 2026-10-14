package com.acme.transport.safety.cameraevent.audit

import kotlin.test.Test
import kotlin.test.assertEquals

class CameraEventAuditViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCameraEventAudit("code-588", "label-182")
        val view = toCameraEventAuditView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCameraEventAuditViews(mutableListOf<CameraEventAudit>(item, item)).size, "views")
    }
}
