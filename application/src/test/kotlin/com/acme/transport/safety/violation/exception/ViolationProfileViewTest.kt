package com.acme.transport.safety.violation.exception

import kotlin.test.Test
import kotlin.test.assertEquals

class ViolationProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newViolationProfile("code-625", "label-815")
        val view = toViolationProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toViolationProfileViews(mutableListOf<ViolationProfile>(item, item)).size, "views")
    }
}
