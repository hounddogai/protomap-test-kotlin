package com.acme.transport.crewing.restbreak.summary

import kotlin.test.Test
import kotlin.test.assertEquals

class RestBreakManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRestBreakManifest("code-867", "label-109")
        val view = toRestBreakManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRestBreakManifestViews(mutableListOf<RestBreakManifest>(item, item)).size, "views")
    }
}
