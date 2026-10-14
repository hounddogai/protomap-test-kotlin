package com.acme.transport.safety.cameraevent.allocation

import kotlin.test.Test
import kotlin.test.assertEquals

class CameraEventAllocationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCameraEventAllocation("code-484", "name-355")
        val view = toCameraEventAllocationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCameraEventAllocationViews(mutableListOf<CameraEventAllocation>(item, item)).size, "views")
    }
}
