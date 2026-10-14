package com.acme.transport.safety.cameraevent.allocation

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientCameraEventPolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientCameraEventPolicy("code-508", "name-760")
        val view = toLenientCameraEventPolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientCameraEventPolicyViews(mutableListOf<LenientCameraEventPolicy>(item, item)).size, "views")
    }
}
