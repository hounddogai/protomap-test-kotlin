package com.acme.transport.linehaul.backhaul.template

import kotlin.test.Test
import kotlin.test.assertEquals

class BackhaulBundleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBackhaulBundle("code-541", "label-602")
        val view = toBackhaulBundleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBackhaulBundleViews(mutableListOf<BackhaulBundle>(item, item)).size, "views")
    }
}
