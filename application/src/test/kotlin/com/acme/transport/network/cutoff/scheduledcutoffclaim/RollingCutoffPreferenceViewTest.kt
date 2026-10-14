package com.acme.transport.network.cutoff.scheduledcutoffclaim

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingCutoffPreferenceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingCutoffPreference("code-509", "name-171")
        val view = toRollingCutoffPreferenceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingCutoffPreferenceViews(mutableListOf<RollingCutoffPreference>(item, item)).size, "views")
    }
}
