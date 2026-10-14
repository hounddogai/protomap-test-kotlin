package com.acme.transport.crewing.restbreak.profile

import kotlin.test.Test
import kotlin.test.assertEquals

class RestBreakProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRestBreakProfile("code-838", "name-947")
        val view = toRestBreakProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRestBreakProfileViews(mutableListOf<RestBreakProfile>(item, item)).size, "views")
    }
}
