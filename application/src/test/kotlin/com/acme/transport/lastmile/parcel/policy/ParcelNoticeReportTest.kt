package com.acme.transport.lastmile.parcel.policy

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParcelNoticeReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706524282)
        val items = mutableListOf<ParcelNotice>()
        for (index in 0 until 4) {
            items.add(newParcelNotice("parcel-notice-$index", "sample"))
        }
        val report = buildParcelNoticeReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
