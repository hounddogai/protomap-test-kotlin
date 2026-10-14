package com.acme.transport.lastmile.locker.preference

import kotlin.test.Test
import kotlin.test.assertEquals

class LockerPreferenceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLockerPreference("code-237", "name-341")
        val view = toLockerPreferenceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLockerPreferenceViews(mutableListOf<LockerPreference>(item, item)).size, "views")
    }
}
