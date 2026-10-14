package com.acme.transport.safety.cameraevent.defaulteventscore

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalCameraEventQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalCameraEventQuota("code-310", "name-721")
        val view = toSeasonalCameraEventQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalCameraEventQuotaViews(mutableListOf<SeasonalCameraEventQuota>(item, item)).size, "views")
    }
}
