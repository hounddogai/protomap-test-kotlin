package com.acme.transport.linehaul.tender.group

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictTenderProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictTenderProfile("code-849", "label-856")
        val view = toStrictTenderProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictTenderProfileViews(mutableListOf<StrictTenderProfile>(item, item)).size, "views")
    }
}
