package com.acme.transport.crewing.swap.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictSwapCheckpointViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictSwapCheckpoint("code-623", "title-530")
        val view = toStrictSwapCheckpointView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictSwapCheckpointViews(mutableListOf<StrictSwapCheckpoint>(item, item)).size, "views")
    }
}
