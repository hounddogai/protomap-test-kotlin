package com.acme.transport.linehaul.trailer.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals

class TrailerRecordViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTrailerRecord("code-143", "label-948")
        val view = toTrailerRecordView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTrailerRecordViews(mutableListOf<TrailerRecord>(item, item)).size, "views")
    }
}
