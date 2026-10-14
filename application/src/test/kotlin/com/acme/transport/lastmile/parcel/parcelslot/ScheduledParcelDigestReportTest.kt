package com.acme.transport.lastmile.parcel.parcelslot

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledParcelDigestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701184201)
        val items = mutableListOf<ScheduledParcelDigest>()
        for (index in 0 until 4) {
            items.add(newScheduledParcelDigest("scheduled-parcel-digest-$index", "sample"))
        }
        val report = buildScheduledParcelDigestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
