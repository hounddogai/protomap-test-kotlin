package com.acme.transport.crewing.swap.bundle

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultSwapCheckpointViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultSwapCheckpoint("code-655", "title-203")
        val view = toDefaultSwapCheckpointView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultSwapCheckpointViews(mutableListOf<DefaultSwapCheckpoint>(item, item)).size, "views")
    }
}
