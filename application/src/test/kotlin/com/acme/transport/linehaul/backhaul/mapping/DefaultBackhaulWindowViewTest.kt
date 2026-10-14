package com.acme.transport.linehaul.backhaul.mapping

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultBackhaulWindowViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultBackhaulWindow("code-795", "label-540")
        val view = toDefaultBackhaulWindowView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultBackhaulWindowViews(mutableListOf<DefaultBackhaulWindow>(item, item)).size, "views")
    }
}
