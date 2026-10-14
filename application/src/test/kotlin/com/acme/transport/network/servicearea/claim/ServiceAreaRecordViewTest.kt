package com.acme.transport.network.servicearea.claim

import kotlin.test.Test
import kotlin.test.assertEquals

class ServiceAreaRecordViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newServiceAreaRecord("code-82", "title-282")
        val view = toServiceAreaRecordView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toServiceAreaRecordViews(mutableListOf<ServiceAreaRecord>(item, item)).size, "views")
    }
}
