package com.acme.transport.crewing.restbreak.breakprofile

import kotlin.test.Test
import kotlin.test.assertEquals

class RestBreakCheckpointViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRestBreakCheckpoint("code-134", "title-913")
        val view = toRestBreakCheckpointView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRestBreakCheckpointViews(mutableListOf<RestBreakCheckpoint>(item, item)).size, "views")
    }
}
