package com.acme.transport.lastmile.drop.digest

import kotlin.test.Test
import kotlin.test.assertEquals

class DropDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDropDigest("code-541", "label-620")
        val view = toDropDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDropDigestViews(mutableListOf<DropDigest>(item, item)).size, "views")
    }
}
