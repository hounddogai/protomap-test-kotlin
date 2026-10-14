package com.acme.transport.network.servicearea.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredServiceAreaRecordViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredServiceAreaRecord("code-876", "name-541")
        val view = toTieredServiceAreaRecordView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredServiceAreaRecordViews(mutableListOf<TieredServiceAreaRecord>(item, item)).size, "views")
    }
}
