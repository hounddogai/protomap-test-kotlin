package com.acme.transport.linehaul.backhaul.primarybackhaulvariance

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalBackhaulPreferenceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalBackhaulPreference("code-704", "label-235")
        val view = toGlobalBackhaulPreferenceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalBackhaulPreferenceViews(mutableListOf<GlobalBackhaulPreference>(item, item)).size, "views")
    }
}
