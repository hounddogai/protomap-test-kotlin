package com.acme.transport.lastmile.drop.exception

import kotlin.test.Test
import kotlin.test.assertEquals

class DropPreferenceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDropPreference("code-812", "label-973")
        val view = toDropPreferenceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDropPreferenceViews(mutableListOf<DropPreference>(item, item)).size, "views")
    }
}
