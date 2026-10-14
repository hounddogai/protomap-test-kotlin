package com.acme.transport.crewing.dutyperiod.schedule

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualDutyPeriodSnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualDutyPeriodSnapshot("code-837", "name-527")
        val view = toManualDutyPeriodSnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualDutyPeriodSnapshotViews(mutableListOf<ManualDutyPeriodSnapshot>(item, item)).size, "views")
    }
}
