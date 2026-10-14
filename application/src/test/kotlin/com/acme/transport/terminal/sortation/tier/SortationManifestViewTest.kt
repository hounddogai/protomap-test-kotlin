package com.acme.transport.terminal.sortation.tier

import kotlin.test.Test
import kotlin.test.assertEquals

class SortationManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSortationManifest("code-371", "label-219")
        val view = toSortationManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSortationManifestViews(mutableListOf<SortationManifest>(item, item)).size, "views")
    }
}
