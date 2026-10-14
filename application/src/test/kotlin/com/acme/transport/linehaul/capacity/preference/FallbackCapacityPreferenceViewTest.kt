package com.acme.transport.linehaul.capacity.preference

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackCapacityPreferenceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackCapacityPreference("code-466", "title-820")
        val view = toFallbackCapacityPreferenceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackCapacityPreferenceViews(mutableListOf<FallbackCapacityPreference>(item, item)).size, "views")
    }
}
