package com.acme.transport.crewing.swap.claim

import kotlin.test.Test
import kotlin.test.assertEquals

class SwapManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSwapManifest("code-775", "title-240")
        val view = toSwapManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSwapManifestViews(mutableListOf<SwapManifest>(item, item)).size, "views")
    }
}
