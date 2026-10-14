package com.acme.transport.safety.cameraevent.rollup

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryCameraEventTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryCameraEventTarget("code-11", "title-96")
        val view = toPrimaryCameraEventTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryCameraEventTargetViews(mutableListOf<PrimaryCameraEventTarget>(item, item)).size, "views")
    }
}
