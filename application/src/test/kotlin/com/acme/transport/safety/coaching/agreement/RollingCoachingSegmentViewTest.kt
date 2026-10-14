package com.acme.transport.safety.coaching.agreement

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingCoachingSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingCoachingSegment("code-791", "title-802")
        val view = toRollingCoachingSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingCoachingSegmentViews(mutableListOf<RollingCoachingSegment>(item, item)).size, "views")
    }
}
