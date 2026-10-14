package com.acme.transport.lastmile.locker.metric

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualLockerMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualLockerMetric("code-296", "title-237")
        val view = toManualLockerMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualLockerMetricViews(mutableListOf<ManualLockerMetric>(item, item)).size, "views")
    }
}
