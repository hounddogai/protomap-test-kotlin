package com.acme.transport.network.servicearea.agreement

import kotlin.test.Test
import kotlin.test.assertEquals

class ServiceAreaDraftViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newServiceAreaDraft("code-350", "name-322")
        val view = toServiceAreaDraftView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toServiceAreaDraftViews(mutableListOf<ServiceAreaDraft>(item, item)).size, "views")
    }
}
