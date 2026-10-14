package com.acme.transport.customs.hold.quota

import kotlin.test.Test
import kotlin.test.assertEquals

class HoldCheckpointViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newHoldCheckpoint("code-720", "name-446")
        val view = toHoldCheckpointView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toHoldCheckpointViews(mutableListOf<HoldCheckpoint>(item, item)).size, "views")
    }
}
