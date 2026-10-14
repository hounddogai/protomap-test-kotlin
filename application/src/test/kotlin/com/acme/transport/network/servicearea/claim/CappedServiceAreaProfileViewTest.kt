package com.acme.transport.network.servicearea.claim

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedServiceAreaProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedServiceAreaProfile("code-787", "name-382")
        val view = toCappedServiceAreaProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedServiceAreaProfileViews(mutableListOf<CappedServiceAreaProfile>(item, item)).size, "views")
    }
}
