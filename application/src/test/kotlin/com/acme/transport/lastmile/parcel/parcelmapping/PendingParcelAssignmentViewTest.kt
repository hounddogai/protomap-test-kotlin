package com.acme.transport.lastmile.parcel.parcelmapping

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingParcelAssignmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingParcelAssignment("code-152", "title-526")
        val view = toPendingParcelAssignmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingParcelAssignmentViews(mutableListOf<PendingParcelAssignment>(item, item)).size, "views")
    }
}
