package com.acme.transport.lastmile.parcel.milestone

import kotlin.test.Test
import kotlin.test.assertEquals

class ParcelSummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newParcelSummary("code-150", "title-219")
        val view = toParcelSummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toParcelSummaryViews(mutableListOf<ParcelSummary>(item, item)).size, "views")
    }
}
