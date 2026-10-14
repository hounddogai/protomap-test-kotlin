package com.acme.transport.fleetops.utilization.variance

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualUtilizationVarianceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700426825)
        val items = mutableListOf<ManualUtilizationVariance>()
        for (index in 0 until 2) {
            items.add(newManualUtilizationVariance("manual-utilization-variance-$index", "sample"))
        }
        val report = buildManualUtilizationVarianceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
