package com.acme.transport.freightpay.accessorial.accessorialscore

import kotlin.test.Test
import kotlin.test.assertEquals

class AccessorialSnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAccessorialSnapshot("code-275", "title-860")
        val view = toAccessorialSnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAccessorialSnapshotViews(mutableListOf<AccessorialSnapshot>(item, item)).size, "views")
    }
}
