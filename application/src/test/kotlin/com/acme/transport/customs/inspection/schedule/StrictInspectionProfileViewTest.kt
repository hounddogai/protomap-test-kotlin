package com.acme.transport.customs.inspection.schedule

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictInspectionProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictInspectionProfile("code-112", "name-277")
        val view = toStrictInspectionProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictInspectionProfileViews(mutableListOf<StrictInspectionProfile>(item, item)).size, "views")
    }
}
