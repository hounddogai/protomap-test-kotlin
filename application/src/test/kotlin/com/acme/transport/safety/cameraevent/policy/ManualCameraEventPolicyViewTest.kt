package com.acme.transport.safety.cameraevent.policy

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualCameraEventPolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualCameraEventPolicy("code-809", "name-433")
        val view = toManualCameraEventPolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualCameraEventPolicyViews(mutableListOf<ManualCameraEventPolicy>(item, item)).size, "views")
    }
}
