package com.acme.transport.safety.cameraevent.group

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalCameraEventEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalCameraEventEntry("code-921", "name-578")
        val view = toSeasonalCameraEventEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalCameraEventEntryViews(mutableListOf<SeasonalCameraEventEntry>(item, item)).size, "views")
    }
}
