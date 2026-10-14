package com.acme.transport.safety.cameraevent.forecast

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredCameraEventTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredCameraEventTarget("code-646", "label-664")
        val view = toTieredCameraEventTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredCameraEventTargetViews(mutableListOf<TieredCameraEventTarget>(item, item)).size, "views")
    }
}
