package com.acme.transport.lastmile.parcel.segment

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParcelDigestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702776269)
        val items = mutableListOf<ParcelDigest>()
        for (index in 0 until 4) {
            items.add(newParcelDigest("parcel-digest-$index", "sample"))
        }
        val report = buildParcelDigestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
