package com.acme.transport.lastmile.parcel.parcelslot

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledParcelDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledParcelDigest("code-931", "label-861")
        val view = toScheduledParcelDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledParcelDigestViews(mutableListOf<ScheduledParcelDigest>(item, item)).size, "views")
    }
}
