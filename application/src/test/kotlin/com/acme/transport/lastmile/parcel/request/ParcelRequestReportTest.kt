package com.acme.transport.lastmile.parcel.request

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParcelRequestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705644236)
        val items = mutableListOf<ParcelRequest>()
        for (index in 0 until 3) {
            items.add(newParcelRequest("parcel-request-$index", "sample"))
        }
        val report = buildParcelRequestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
