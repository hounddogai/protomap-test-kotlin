package com.acme.transport.network.servicearea.manualareathreshold

import kotlin.test.Test
import kotlin.test.assertEquals

class ServiceAreaPreferenceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newServiceAreaPreference("code-365", "label-125")
        val view = toServiceAreaPreferenceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toServiceAreaPreferenceViews(mutableListOf<ServiceAreaPreference>(item, item)).size, "views")
    }
}
