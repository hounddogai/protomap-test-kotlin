package com.acme.transport.network.zone.zonequota

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalZonePolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalZonePolicy("code-646", "name-204")
        val view = toSeasonalZonePolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalZonePolicyViews(mutableListOf<SeasonalZonePolicy>(item, item)).size, "views")
    }
}
