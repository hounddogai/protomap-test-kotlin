package com.acme.transport.lastmile.parcel.segment

import kotlin.test.Test
import kotlin.test.assertEquals

class ParcelDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newParcelDigest("code-556", "title-262")
        val view = toParcelDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toParcelDigestViews(mutableListOf<ParcelDigest>(item, item)).size, "views")
    }
}
