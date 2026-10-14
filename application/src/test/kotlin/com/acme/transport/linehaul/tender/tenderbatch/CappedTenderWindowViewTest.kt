package com.acme.transport.linehaul.tender.tenderbatch

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedTenderWindowViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedTenderWindow("code-763", "name-788")
        val view = toCappedTenderWindowView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedTenderWindowViews(mutableListOf<CappedTenderWindow>(item, item)).size, "views")
    }
}
