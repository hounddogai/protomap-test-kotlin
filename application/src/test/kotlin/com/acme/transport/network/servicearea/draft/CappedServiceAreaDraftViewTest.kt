package com.acme.transport.network.servicearea.draft

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedServiceAreaDraftViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedServiceAreaDraft("code-130", "label-282")
        val view = toCappedServiceAreaDraftView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedServiceAreaDraftViews(mutableListOf<CappedServiceAreaDraft>(item, item)).size, "views")
    }
}
