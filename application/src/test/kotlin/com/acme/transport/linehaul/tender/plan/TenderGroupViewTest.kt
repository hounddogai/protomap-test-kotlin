package com.acme.transport.linehaul.tender.plan

import kotlin.test.Test
import kotlin.test.assertEquals

class TenderGroupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTenderGroup("code-255", "title-461")
        val view = toTenderGroupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTenderGroupViews(mutableListOf<TenderGroup>(item, item)).size, "views")
    }
}
