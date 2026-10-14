package com.acme.transport.terminal.traileryard.profile

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalTrailerYardTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalTrailerYardTemplate("code-347", "title-160")
        val view = toGlobalTrailerYardTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalTrailerYardTemplateViews(mutableListOf<GlobalTrailerYardTemplate>(item, item)).size, "views")
    }
}
