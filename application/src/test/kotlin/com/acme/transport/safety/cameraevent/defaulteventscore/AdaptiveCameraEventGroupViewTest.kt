package com.acme.transport.safety.cameraevent.defaulteventscore

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveCameraEventGroupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveCameraEventGroup("code-849", "title-775")
        val view = toAdaptiveCameraEventGroupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveCameraEventGroupViews(mutableListOf<AdaptiveCameraEventGroup>(item, item)).size, "views")
    }
}
