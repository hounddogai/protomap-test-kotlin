package com.acme.transport.crewing.dutyperiod.globalperiodsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveDutyPeriodCheckpointViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveDutyPeriodCheckpoint("code-860", "title-576")
        val view = toAdaptiveDutyPeriodCheckpointView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveDutyPeriodCheckpointViews(mutableListOf<AdaptiveDutyPeriodCheckpoint>(item, item)).size, "views")
    }
}
