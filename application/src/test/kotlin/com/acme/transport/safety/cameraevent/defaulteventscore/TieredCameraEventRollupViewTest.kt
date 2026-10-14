package com.acme.transport.safety.cameraevent.defaulteventscore

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredCameraEventRollupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredCameraEventRollup("code-836", "label-430")
        val view = toTieredCameraEventRollupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredCameraEventRollupViews(mutableListOf<TieredCameraEventRollup>(item, item)).size, "views")
    }
}
