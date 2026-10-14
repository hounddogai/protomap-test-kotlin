package com.acme.transport.linehaul.trailer.exception

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictTrailerManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictTrailerManifest("code-524", "title-351")
        val view = toStrictTrailerManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictTrailerManifestViews(mutableListOf<StrictTrailerManifest>(item, item)).size, "views")
    }
}
