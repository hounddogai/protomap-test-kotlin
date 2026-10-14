package com.acme.transport.crewing.restbreak.assignment

import kotlin.test.Test
import kotlin.test.assertEquals

class RestBreakOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRestBreakOverride("code-631", "name-125")
        val view = toRestBreakOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRestBreakOverrideViews(mutableListOf<RestBreakOverride>(item, item)).size, "views")
    }
}
