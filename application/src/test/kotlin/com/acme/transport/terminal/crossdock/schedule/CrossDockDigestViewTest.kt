package com.acme.transport.terminal.crossdock.schedule

import kotlin.test.Test
import kotlin.test.assertEquals

class CrossDockDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCrossDockDigest("code-572", "title-879")
        val view = toCrossDockDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCrossDockDigestViews(mutableListOf<CrossDockDigest>(item, item)).size, "views")
    }
}
