package com.acme.transport.safety.cameraevent.evententry

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryCameraEventDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryCameraEventDigest("code-912", "title-16")
        val view = toPrimaryCameraEventDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryCameraEventDigestViews(mutableListOf<PrimaryCameraEventDigest>(item, item)).size, "views")
    }
}
