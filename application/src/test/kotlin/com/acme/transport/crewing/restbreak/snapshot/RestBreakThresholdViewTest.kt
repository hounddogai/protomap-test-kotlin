package com.acme.transport.crewing.restbreak.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals

class RestBreakThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRestBreakThreshold("code-653", "name-748")
        val view = toRestBreakThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRestBreakThresholdViews(mutableListOf<RestBreakThreshold>(item, item)).size, "views")
    }
}
