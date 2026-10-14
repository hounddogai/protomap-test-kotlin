package com.acme.transport.lastmile.parcel.rollup

import kotlin.test.Test
import kotlin.test.assertEquals

class ParcelTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newParcelTemplate("code-607", "name-158")
        val view = toParcelTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toParcelTemplateViews(mutableListOf<ParcelTemplate>(item, item)).size, "views")
    }
}
