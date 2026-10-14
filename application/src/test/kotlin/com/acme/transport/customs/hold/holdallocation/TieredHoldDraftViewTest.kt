package com.acme.transport.customs.hold.holdallocation

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredHoldDraftViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredHoldDraft("code-67", "label-629")
        val view = toTieredHoldDraftView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredHoldDraftViews(mutableListOf<TieredHoldDraft>(item, item)).size, "views")
    }
}
