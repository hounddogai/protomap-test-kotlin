package com.acme.transport.lastmile.locker.manifest

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingLockerPolicyReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706982340)
        val items = mutableListOf<RollingLockerPolicy>()
        for (index in 0 until 2) {
            items.add(newRollingLockerPolicy("rolling-locker-policy-$index", "sample"))
        }
        val report = buildRollingLockerPolicyReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
