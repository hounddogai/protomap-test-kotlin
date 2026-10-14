package com.acme.transport.safety.nearmiss.preference

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualNearMissProfileReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707946133)
        val items = mutableListOf<ManualNearMissProfile>()
        for (index in 0 until 2) {
            items.add(newManualNearMissProfile("manual-near-miss-profile-$index", "sample"))
        }
        val report = buildManualNearMissProfileReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
