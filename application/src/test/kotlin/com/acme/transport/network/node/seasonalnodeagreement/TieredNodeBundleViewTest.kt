package com.acme.transport.network.node.seasonalnodeagreement

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredNodeBundleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredNodeBundle("code-46", "name-41")
        val view = toTieredNodeBundleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredNodeBundleViews(mutableListOf<TieredNodeBundle>(item, item)).size, "views")
    }
}
