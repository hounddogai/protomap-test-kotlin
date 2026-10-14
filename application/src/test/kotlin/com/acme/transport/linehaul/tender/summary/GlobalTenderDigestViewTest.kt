package com.acme.transport.linehaul.tender.summary

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalTenderDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalTenderDigest("code-342", "title-212")
        val view = toGlobalTenderDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalTenderDigestViews(mutableListOf<GlobalTenderDigest>(item, item)).size, "views")
    }
}
