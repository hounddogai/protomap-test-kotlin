package com.acme.transport.linehaul.trailer.policy

import kotlin.test.Test
import kotlin.test.assertEquals

class TrailerBundleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTrailerBundle("code-769", "name-397")
        val view = toTrailerBundleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTrailerBundleViews(mutableListOf<TrailerBundle>(item, item)).size, "views")
    }
}
