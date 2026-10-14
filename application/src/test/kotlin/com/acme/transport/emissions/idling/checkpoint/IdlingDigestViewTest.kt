package com.acme.transport.emissions.idling.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals

class IdlingDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newIdlingDigest("code-992", "label-921")
        val view = toIdlingDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toIdlingDigestViews(mutableListOf<IdlingDigest>(item, item)).size, "views")
    }
}
