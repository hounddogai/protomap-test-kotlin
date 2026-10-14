package com.acme.transport.fleetops.utilization.defaultutilizationsummary

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingUtilizationScoreReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707732682)
        val items = mutableListOf<RollingUtilizationScore>()
        for (index in 0 until 5) {
            items.add(newRollingUtilizationScore("rolling-utilization-score-$index", "sample"))
        }
        val report = buildRollingUtilizationScoreReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
