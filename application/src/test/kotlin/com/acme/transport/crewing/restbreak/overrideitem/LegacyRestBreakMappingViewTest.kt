package com.acme.transport.crewing.restbreak.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyRestBreakMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyRestBreakMapping("code-834", "title-469")
        val view = toLegacyRestBreakMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyRestBreakMappingViews(mutableListOf<LegacyRestBreakMapping>(item, item)).size, "views")
    }
}
