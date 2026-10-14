package com.acme.transport.safety.cameraevent.group

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultCameraEventGroupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultCameraEventGroup("code-193", "label-833")
        val view = toDefaultCameraEventGroupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultCameraEventGroupViews(mutableListOf<DefaultCameraEventGroup>(item, item)).size, "views")
    }
}
