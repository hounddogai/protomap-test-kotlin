package com.acme.transport.safety.violation.cycle

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalViolationProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalViolationProfile("code-389", "name-67")
        val view = toRegionalViolationProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalViolationProfileViews(mutableListOf<RegionalViolationProfile>(item, item)).size, "views")
    }
}
