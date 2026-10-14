package com.acme.transport.network.zone.group

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedZoneGroupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedZoneGroup("code-591", "name-699")
        val view = toCappedZoneGroupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedZoneGroupViews(mutableListOf<CappedZoneGroup>(item, item)).size, "views")
    }
}
