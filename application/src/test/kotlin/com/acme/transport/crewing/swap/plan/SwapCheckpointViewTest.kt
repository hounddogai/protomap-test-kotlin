package com.acme.transport.crewing.swap.plan

import kotlin.test.Test
import kotlin.test.assertEquals

class SwapCheckpointViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSwapCheckpoint("code-593", "name-528")
        val view = toSwapCheckpointView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSwapCheckpointViews(mutableListOf<SwapCheckpoint>(item, item)).size, "views")
    }
}
