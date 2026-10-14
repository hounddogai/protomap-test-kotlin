package com.acme.transport.terminal.sortation.request

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictSortationManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictSortationManifest("code-552", "label-376")
        val view = toStrictSortationManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictSortationManifestViews(mutableListOf<StrictSortationManifest>(item, item)).size, "views")
    }
}
