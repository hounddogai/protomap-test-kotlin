package com.acme.transport.lastmile.parcel.parcelbaseline

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedParcelEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedParcelEntry("code-463", "name-976")
        val view = toCappedParcelEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedParcelEntryViews(mutableListOf<CappedParcelEntry>(item, item)).size, "views")
    }
}
