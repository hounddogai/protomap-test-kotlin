package com.acme.transport.customs.clearance.clearancerequest

import kotlin.test.Test
import kotlin.test.assertEquals

class ClearanceDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newClearanceDigest("code-329", "title-667")
        val view = toClearanceDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toClearanceDigestViews(mutableListOf<ClearanceDigest>(item, item)).size, "views")
    }
}
