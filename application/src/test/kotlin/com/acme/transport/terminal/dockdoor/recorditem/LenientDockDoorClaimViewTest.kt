package com.acme.transport.terminal.dockdoor.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientDockDoorClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientDockDoorClaim("code-996", "name-428")
        val view = toLenientDockDoorClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientDockDoorClaimViews(mutableListOf<LenientDockDoorClaim>(item, item)).size, "views")
    }
}
