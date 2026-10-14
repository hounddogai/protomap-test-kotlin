package com.acme.transport.lastmile.parcel.claim

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingParcelOverrideReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704327513)
        val items = mutableListOf<PendingParcelOverride>()
        for (index in 0 until 4) {
            items.add(newPendingParcelOverride("pending-parcel-override-$index", "sample"))
        }
        val report = buildPendingParcelOverrideReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
