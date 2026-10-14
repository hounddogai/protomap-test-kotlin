package com.acme.transport.lastmile.drop.dropmilestone

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackDropDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackDropDigest("code-596", "label-489")
        val view = toFallbackDropDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackDropDigestViews(mutableListOf<FallbackDropDigest>(item, item)).size, "views")
    }
}
