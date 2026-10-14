package com.acme.transport.linehaul.tender.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientTenderCheckpointViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientTenderCheckpoint("code-432", "label-303")
        val view = toLenientTenderCheckpointView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientTenderCheckpointViews(mutableListOf<LenientTenderCheckpoint>(item, item)).size, "views")
    }
}
