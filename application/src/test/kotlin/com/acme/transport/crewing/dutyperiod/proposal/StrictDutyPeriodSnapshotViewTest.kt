package com.acme.transport.crewing.dutyperiod.proposal

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictDutyPeriodSnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictDutyPeriodSnapshot("code-90", "name-802")
        val view = toStrictDutyPeriodSnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictDutyPeriodSnapshotViews(mutableListOf<StrictDutyPeriodSnapshot>(item, item)).size, "views")
    }
}
