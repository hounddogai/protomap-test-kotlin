package com.acme.transport.crewing.availability.claim

import kotlin.test.Test
import kotlin.test.assertEquals

class AvailabilityPolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAvailabilityPolicy("code-248", "title-644")
        val view = toAvailabilityPolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAvailabilityPolicyViews(mutableListOf<AvailabilityPolicy>(item, item)).size, "views")
    }
}
