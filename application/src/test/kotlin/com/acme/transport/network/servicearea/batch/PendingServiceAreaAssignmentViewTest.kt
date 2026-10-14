package com.acme.transport.network.servicearea.batch

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingServiceAreaAssignmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingServiceAreaAssignment("code-597", "title-420")
        val view = toPendingServiceAreaAssignmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingServiceAreaAssignmentViews(mutableListOf<PendingServiceAreaAssignment>(item, item)).size, "views")
    }
}
