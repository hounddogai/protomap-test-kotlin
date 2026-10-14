package com.acme.transport.crewing.qualification.template

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedQualificationSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedQualificationSegment("code-775", "title-654")
        val view = toCappedQualificationSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedQualificationSegmentViews(mutableListOf<CappedQualificationSegment>(item, item)).size, "views")
    }
}
