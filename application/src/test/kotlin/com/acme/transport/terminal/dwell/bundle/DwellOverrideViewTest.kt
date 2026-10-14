package com.acme.transport.terminal.dwell.bundle

import kotlin.test.Test
import kotlin.test.assertEquals

class DwellOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDwellOverride("code-536", "title-333")
        val view = toDwellOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDwellOverrideViews(mutableListOf<DwellOverride>(item, item)).size, "views")
    }
}
