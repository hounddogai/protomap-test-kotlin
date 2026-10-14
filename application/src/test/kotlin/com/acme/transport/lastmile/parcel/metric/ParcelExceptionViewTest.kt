package com.acme.transport.lastmile.parcel.metric

import kotlin.test.Test
import kotlin.test.assertEquals

class ParcelExceptionViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newParcelException("code-621", "label-842")
        val view = toParcelExceptionView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toParcelExceptionViews(mutableListOf<ParcelException>(item, item)).size, "views")
    }
}
