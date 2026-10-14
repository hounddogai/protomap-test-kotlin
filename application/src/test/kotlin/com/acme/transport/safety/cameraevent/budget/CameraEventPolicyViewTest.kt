package com.acme.transport.safety.cameraevent.budget

import kotlin.test.Test
import kotlin.test.assertEquals

class CameraEventPolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCameraEventPolicy("code-236", "name-900")
        val view = toCameraEventPolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCameraEventPolicyViews(mutableListOf<CameraEventPolicy>(item, item)).size, "views")
    }
}
