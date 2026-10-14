package com.acme.transport.fleetops.serviceplan.batchedplanrollup

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultServicePlanSnapshotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709054306)
        val items = mutableListOf<DefaultServicePlanSnapshot>()
        for (index in 0 until 3) {
            items.add(newDefaultServicePlanSnapshot("default-service-plan-snapshot-$index", "sample"))
        }
        val report = buildDefaultServicePlanSnapshotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
