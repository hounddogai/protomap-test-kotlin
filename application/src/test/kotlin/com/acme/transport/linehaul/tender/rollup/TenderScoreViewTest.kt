package com.acme.transport.linehaul.tender.rollup

import kotlin.test.Test
import kotlin.test.assertEquals

class TenderScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTenderScore("code-681", "label-714")
        val view = toTenderScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTenderScoreViews(mutableListOf<TenderScore>(item, item)).size, "views")
    }
}
