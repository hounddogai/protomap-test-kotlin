package com.acme.transport.crewing.dutyperiod.proposal

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DutyPeriodDigestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703097747)
        val items = mutableListOf<DutyPeriodDigest>()
        for (index in 0 until 5) {
            items.add(newDutyPeriodDigest("duty-period-digest-$index", "sample"))
        }
        val report = buildDutyPeriodDigestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
