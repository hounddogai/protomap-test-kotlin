package com.acme.transport.lastmile.parcel.profile

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedParcelDigestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705441222)
        val items = mutableListOf<CappedParcelDigest>()
        for (index in 0 until 3) {
            items.add(newCappedParcelDigest("capped-parcel-digest-$index", "sample"))
        }
        val report = buildCappedParcelDigestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
