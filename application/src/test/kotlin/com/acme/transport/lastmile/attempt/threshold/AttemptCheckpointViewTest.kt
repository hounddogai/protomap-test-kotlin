package com.acme.transport.lastmile.attempt.threshold

import kotlin.test.Test
import kotlin.test.assertEquals

class AttemptCheckpointViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAttemptCheckpoint("code-153", "title-995")
        val view = toAttemptCheckpointView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAttemptCheckpointViews(mutableListOf<AttemptCheckpoint>(item, item)).size, "views")
    }
}
