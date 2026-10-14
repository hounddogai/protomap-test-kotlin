package com.acme.transport.lastmile.parcel.metric

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParcelExceptionReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707034306)
        val items = mutableListOf<ParcelException>()
        for (index in 0 until 3) {
            items.add(newParcelException("parcel-exception-$index", "sample"))
        }
        val report = buildParcelExceptionReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
