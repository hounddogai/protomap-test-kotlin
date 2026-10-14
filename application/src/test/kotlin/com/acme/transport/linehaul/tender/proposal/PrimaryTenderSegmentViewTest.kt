package com.acme.transport.linehaul.tender.proposal

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryTenderSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryTenderSegment("code-477", "title-723")
        val view = toPrimaryTenderSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryTenderSegmentViews(mutableListOf<PrimaryTenderSegment>(item, item)).size, "views")
    }
}
