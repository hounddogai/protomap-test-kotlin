package com.acme.transport.safety.cameraevent.globaleventaudit

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveCameraEventAllocationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveCameraEventAllocation("code-542", "label-919")
        val view = toAdaptiveCameraEventAllocationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveCameraEventAllocationViews(mutableListOf<AdaptiveCameraEventAllocation>(item, item)).size, "views")
    }
}
