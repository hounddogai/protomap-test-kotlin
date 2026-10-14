package com.acme.transport.linehaul.relay.assignment

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveRelayAssignmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveRelayAssignment("code-442", "title-579")
        val view = toAdaptiveRelayAssignmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveRelayAssignmentViews(mutableListOf<AdaptiveRelayAssignment>(item, item)).size, "views")
    }
}
