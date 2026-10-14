package com.acme.transport.linehaul.tender.tenderrollup

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictTenderRollupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictTenderRollup("code-553", "label-340")
        val view = toStrictTenderRollupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictTenderRollupViews(mutableListOf<StrictTenderRollup>(item, item)).size, "views")
    }
}
