package com.acme.transport.safety.cameraevent.entry

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedCameraEventEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedCameraEventEntry("code-448", "name-852")
        val view = toCappedCameraEventEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedCameraEventEntryViews(mutableListOf<CappedCameraEventEntry>(item, item)).size, "views")
    }
}
