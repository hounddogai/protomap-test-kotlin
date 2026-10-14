package com.acme.transport.customs.hold.digest

import kotlin.test.Test
import kotlin.test.assertEquals

class HoldDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newHoldDigest("code-146", "label-679")
        val view = toHoldDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toHoldDigestViews(mutableListOf<HoldDigest>(item, item)).size, "views")
    }
}
