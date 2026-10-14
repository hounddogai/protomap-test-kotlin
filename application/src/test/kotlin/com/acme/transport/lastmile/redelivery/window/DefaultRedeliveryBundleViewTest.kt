package com.acme.transport.lastmile.redelivery.window

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultRedeliveryBundleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultRedeliveryBundle("code-943", "name-706")
        val view = toDefaultRedeliveryBundleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultRedeliveryBundleViews(mutableListOf<DefaultRedeliveryBundle>(item, item)).size, "views")
    }
}
