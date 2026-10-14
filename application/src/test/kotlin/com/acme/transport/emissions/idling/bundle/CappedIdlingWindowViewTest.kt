package com.acme.transport.emissions.idling.bundle

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedIdlingWindowViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedIdlingWindow("code-175", "title-812")
        val view = toCappedIdlingWindowView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedIdlingWindowViews(mutableListOf<CappedIdlingWindow>(item, item)).size, "views")
    }
}
