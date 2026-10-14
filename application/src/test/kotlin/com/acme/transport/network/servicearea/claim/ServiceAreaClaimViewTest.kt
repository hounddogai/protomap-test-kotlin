package com.acme.transport.network.servicearea.claim

import kotlin.test.Test
import kotlin.test.assertEquals

class ServiceAreaClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newServiceAreaClaim("code-135", "title-601")
        val view = toServiceAreaClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toServiceAreaClaimViews(mutableListOf<ServiceAreaClaim>(item, item)).size, "views")
    }
}
