package com.acme.transport.lastmile.attempt.profile

import kotlin.test.Test
import kotlin.test.assertEquals

class AttemptProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAttemptProfile("code-726", "name-83")
        val view = toAttemptProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAttemptProfileViews(mutableListOf<AttemptProfile>(item, item)).size, "views")
    }
}
