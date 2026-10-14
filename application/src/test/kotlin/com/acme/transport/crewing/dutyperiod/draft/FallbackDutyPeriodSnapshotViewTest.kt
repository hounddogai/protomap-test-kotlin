package com.acme.transport.crewing.dutyperiod.draft

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackDutyPeriodSnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackDutyPeriodSnapshot("code-168", "label-254")
        val view = toFallbackDutyPeriodSnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackDutyPeriodSnapshotViews(mutableListOf<FallbackDutyPeriodSnapshot>(item, item)).size, "views")
    }
}
