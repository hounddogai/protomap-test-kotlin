package com.acme.transport.safety.coaching.milestone

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyCoachingQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyCoachingQuota("code-719", "label-768")
        val view = toLegacyCoachingQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyCoachingQuotaViews(mutableListOf<LegacyCoachingQuota>(item, item)).size, "views")
    }
}
