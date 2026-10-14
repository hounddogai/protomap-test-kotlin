package com.acme.transport.safety.nearmiss.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals

class NearMissRecordViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newNearMissRecord("code-553", "title-908")
        val view = toNearMissRecordView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toNearMissRecordViews(mutableListOf<NearMissRecord>(item, item)).size, "views")
    }
}
