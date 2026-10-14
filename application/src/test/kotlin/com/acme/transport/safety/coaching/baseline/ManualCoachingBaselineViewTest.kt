package com.acme.transport.safety.coaching.baseline

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualCoachingBaselineViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualCoachingBaseline("code-971", "name-519")
        val view = toManualCoachingBaselineView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualCoachingBaselineViews(mutableListOf<ManualCoachingBaseline>(item, item)).size, "views")
    }
}
