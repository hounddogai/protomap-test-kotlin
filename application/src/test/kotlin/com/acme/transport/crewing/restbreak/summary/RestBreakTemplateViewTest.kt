package com.acme.transport.crewing.restbreak.summary

import kotlin.test.Test
import kotlin.test.assertEquals

class RestBreakTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRestBreakTemplate("code-32", "name-652")
        val view = toRestBreakTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRestBreakTemplateViews(mutableListOf<RestBreakTemplate>(item, item)).size, "views")
    }
}
