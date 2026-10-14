package com.acme.transport.crewing.qualification.cycle

import kotlin.test.Test
import kotlin.test.assertEquals

class QualificationSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newQualificationSegment("code-698", "name-847")
        val view = toQualificationSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toQualificationSegmentViews(mutableListOf<QualificationSegment>(item, item)).size, "views")
    }
}
