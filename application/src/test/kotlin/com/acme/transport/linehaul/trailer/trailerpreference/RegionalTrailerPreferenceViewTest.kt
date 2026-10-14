package com.acme.transport.linehaul.trailer.trailerpreference

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalTrailerPreferenceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalTrailerPreference("code-825", "title-157")
        val view = toRegionalTrailerPreferenceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalTrailerPreferenceViews(mutableListOf<RegionalTrailerPreference>(item, item)).size, "views")
    }
}
