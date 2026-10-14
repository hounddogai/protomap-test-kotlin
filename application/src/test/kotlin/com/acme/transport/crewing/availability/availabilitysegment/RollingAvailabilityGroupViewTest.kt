package com.acme.transport.crewing.availability.availabilitysegment

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingAvailabilityGroupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingAvailabilityGroup("code-810", "title-593")
        val view = toRollingAvailabilityGroupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingAvailabilityGroupViews(mutableListOf<RollingAvailabilityGroup>(item, item)).size, "views")
    }
}
