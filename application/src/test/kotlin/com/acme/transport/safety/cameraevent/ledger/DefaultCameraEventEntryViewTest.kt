package com.acme.transport.safety.cameraevent.ledger

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultCameraEventEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultCameraEventEntry("code-485", "label-530")
        val view = toDefaultCameraEventEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultCameraEventEntryViews(mutableListOf<DefaultCameraEventEntry>(item, item)).size, "views")
    }
}
