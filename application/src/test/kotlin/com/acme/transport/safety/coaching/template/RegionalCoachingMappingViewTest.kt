package com.acme.transport.safety.coaching.template

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalCoachingMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalCoachingMapping("code-7", "title-808")
        val view = toRegionalCoachingMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalCoachingMappingViews(mutableListOf<RegionalCoachingMapping>(item, item)).size, "views")
    }
}
