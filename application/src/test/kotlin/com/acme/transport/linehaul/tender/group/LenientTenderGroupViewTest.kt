package com.acme.transport.linehaul.tender.group

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientTenderGroupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientTenderGroup("code-567", "title-895")
        val view = toLenientTenderGroupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientTenderGroupViews(mutableListOf<LenientTenderGroup>(item, item)).size, "views")
    }
}
