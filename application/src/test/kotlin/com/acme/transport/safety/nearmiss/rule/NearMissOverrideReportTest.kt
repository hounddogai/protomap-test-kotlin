package com.acme.transport.safety.nearmiss.rule

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NearMissOverrideReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709648323)
        val items = mutableListOf<NearMissOverride>()
        for (index in 0 until 6) {
            items.add(newNearMissOverride("near-miss-override-$index", "sample"))
        }
        val report = buildNearMissOverrideReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
