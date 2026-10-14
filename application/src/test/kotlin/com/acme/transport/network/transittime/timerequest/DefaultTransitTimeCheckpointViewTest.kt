package com.acme.transport.network.transittime.timerequest

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultTransitTimeCheckpointViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultTransitTimeCheckpoint("code-153", "name-512")
        val view = toDefaultTransitTimeCheckpointView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultTransitTimeCheckpointViews(mutableListOf<DefaultTransitTimeCheckpoint>(item, item)).size, "views")
    }
}
