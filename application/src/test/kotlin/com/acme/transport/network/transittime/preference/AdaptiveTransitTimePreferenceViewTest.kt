package com.acme.transport.network.transittime.preference

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveTransitTimePreferenceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveTransitTimePreference("code-664", "label-890")
        val view = toAdaptiveTransitTimePreferenceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveTransitTimePreferenceViews(mutableListOf<AdaptiveTransitTimePreference>(item, item)).size, "views")
    }
}
