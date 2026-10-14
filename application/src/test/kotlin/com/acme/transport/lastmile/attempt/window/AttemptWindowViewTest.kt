package com.acme.transport.lastmile.attempt.window

import kotlin.test.Test
import kotlin.test.assertEquals

class AttemptWindowViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAttemptWindow("code-190", "label-728")
        val view = toAttemptWindowView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAttemptWindowViews(mutableListOf<AttemptWindow>(item, item)).size, "views")
    }
}
