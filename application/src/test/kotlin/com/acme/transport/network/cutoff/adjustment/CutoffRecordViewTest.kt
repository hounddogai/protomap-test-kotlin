package com.acme.transport.network.cutoff.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals

class CutoffRecordViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCutoffRecord("code-993", "label-452")
        val view = toCutoffRecordView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCutoffRecordViews(mutableListOf<CutoffRecord>(item, item)).size, "views")
    }
}
