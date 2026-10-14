package com.acme.transport.lastmile.parcel.parcelcycle

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalParcelAssignmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalParcelAssignment("code-691", "name-304")
        val view = toGlobalParcelAssignmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalParcelAssignmentViews(mutableListOf<GlobalParcelAssignment>(item, item)).size, "views")
    }
}
