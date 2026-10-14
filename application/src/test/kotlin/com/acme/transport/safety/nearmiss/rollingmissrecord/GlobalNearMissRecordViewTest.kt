package com.acme.transport.safety.nearmiss.rollingmissrecord

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalNearMissRecordViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalNearMissRecord("code-877", "name-820")
        val view = toGlobalNearMissRecordView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalNearMissRecordViews(mutableListOf<GlobalNearMissRecord>(item, item)).size, "views")
    }
}
