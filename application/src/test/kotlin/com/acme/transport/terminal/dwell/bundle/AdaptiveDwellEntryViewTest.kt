package com.acme.transport.terminal.dwell.bundle

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveDwellEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveDwellEntry("code-214", "name-51")
        val view = toAdaptiveDwellEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveDwellEntryViews(mutableListOf<AdaptiveDwellEntry>(item, item)).size, "views")
    }
}
