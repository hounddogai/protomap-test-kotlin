package com.acme.transport.terminal.crossdock.exception

import kotlin.test.Test
import kotlin.test.assertEquals

class CrossDockPreferenceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCrossDockPreference("code-350", "title-600")
        val view = toCrossDockPreferenceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCrossDockPreferenceViews(mutableListOf<CrossDockPreference>(item, item)).size, "views")
    }
}
