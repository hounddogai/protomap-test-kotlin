package com.acme.transport.network.hub.exception

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredHubExceptionViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredHubException("code-123", "name-971")
        val view = toTieredHubExceptionView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredHubExceptionViews(mutableListOf<TieredHubException>(item, item)).size, "views")
    }
}
