package com.acme.transport.emissions.idling.idlingsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalIdlingWindowViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalIdlingWindow("code-420", "label-748")
        val view = toSeasonalIdlingWindowView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalIdlingWindowViews(mutableListOf<SeasonalIdlingWindow>(item, item)).size, "views")
    }
}
