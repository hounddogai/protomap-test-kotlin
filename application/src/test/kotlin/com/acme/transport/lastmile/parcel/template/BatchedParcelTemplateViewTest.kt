package com.acme.transport.lastmile.parcel.template

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedParcelTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedParcelTemplate("code-457", "label-672")
        val view = toBatchedParcelTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedParcelTemplateViews(mutableListOf<BatchedParcelTemplate>(item, item)).size, "views")
    }
}
