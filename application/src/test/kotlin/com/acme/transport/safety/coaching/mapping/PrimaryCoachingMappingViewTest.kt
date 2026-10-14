package com.acme.transport.safety.coaching.mapping

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryCoachingMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryCoachingMapping("code-31", "title-516")
        val view = toPrimaryCoachingMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryCoachingMappingViews(mutableListOf<PrimaryCoachingMapping>(item, item)).size, "views")
    }
}
