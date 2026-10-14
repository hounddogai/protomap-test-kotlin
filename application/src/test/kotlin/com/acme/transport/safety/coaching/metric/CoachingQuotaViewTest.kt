package com.acme.transport.safety.coaching.metric

import kotlin.test.Test
import kotlin.test.assertEquals

class CoachingQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCoachingQuota("code-118", "title-572")
        val view = toCoachingQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCoachingQuotaViews(mutableListOf<CoachingQuota>(item, item)).size, "views")
    }
}
