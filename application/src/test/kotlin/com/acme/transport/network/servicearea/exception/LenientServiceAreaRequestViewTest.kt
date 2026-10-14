package com.acme.transport.network.servicearea.exception

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientServiceAreaRequestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientServiceAreaRequest("code-387", "label-744")
        val view = toLenientServiceAreaRequestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientServiceAreaRequestViews(mutableListOf<LenientServiceAreaRequest>(item, item)).size, "views")
    }
}
