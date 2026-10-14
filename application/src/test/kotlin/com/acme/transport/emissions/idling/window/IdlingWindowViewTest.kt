package com.acme.transport.emissions.idling.window

import kotlin.test.Test
import kotlin.test.assertEquals

class IdlingWindowViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newIdlingWindow("code-520", "title-166")
        val view = toIdlingWindowView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toIdlingWindowViews(mutableListOf<IdlingWindow>(item, item)).size, "views")
    }
}
