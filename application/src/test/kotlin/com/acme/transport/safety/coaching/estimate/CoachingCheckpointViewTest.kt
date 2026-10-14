package com.acme.transport.safety.coaching.estimate

import kotlin.test.Test
import kotlin.test.assertEquals

class CoachingCheckpointViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCoachingCheckpoint("code-337", "name-185")
        val view = toCoachingCheckpointView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCoachingCheckpointViews(mutableListOf<CoachingCheckpoint>(item, item)).size, "views")
    }
}
