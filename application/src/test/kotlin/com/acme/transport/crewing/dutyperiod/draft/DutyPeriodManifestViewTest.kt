package com.acme.transport.crewing.dutyperiod.draft

import kotlin.test.Test
import kotlin.test.assertEquals

class DutyPeriodManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDutyPeriodManifest("code-489", "name-818")
        val view = toDutyPeriodManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDutyPeriodManifestViews(mutableListOf<DutyPeriodManifest>(item, item)).size, "views")
    }
}
