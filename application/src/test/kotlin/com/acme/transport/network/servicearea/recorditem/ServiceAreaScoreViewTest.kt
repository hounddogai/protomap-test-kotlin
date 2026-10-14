package com.acme.transport.network.servicearea.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals

class ServiceAreaScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newServiceAreaScore("code-665", "label-884")
        val view = toServiceAreaScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toServiceAreaScoreViews(mutableListOf<ServiceAreaScore>(item, item)).size, "views")
    }
}
