package com.acme.transport.safety.coaching.template

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualCoachingCheckpointViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualCoachingCheckpoint("code-354", "label-315")
        val view = toManualCoachingCheckpointView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualCoachingCheckpointViews(mutableListOf<ManualCoachingCheckpoint>(item, item)).size, "views")
    }
}
