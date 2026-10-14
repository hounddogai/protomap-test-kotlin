package com.acme.transport.terminal.sortation.bundle

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalSortationBundleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalSortationBundle("code-289", "title-808")
        val view = toGlobalSortationBundleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalSortationBundleViews(mutableListOf<GlobalSortationBundle>(item, item)).size, "views")
    }
}
