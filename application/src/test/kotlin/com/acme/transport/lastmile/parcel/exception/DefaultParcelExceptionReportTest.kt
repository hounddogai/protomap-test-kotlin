package com.acme.transport.lastmile.parcel.exception

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultParcelExceptionReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708467746)
        val items = mutableListOf<DefaultParcelException>()
        for (index in 0 until 2) {
            items.add(newDefaultParcelException("default-parcel-exception-$index", "sample"))
        }
        val report = buildDefaultParcelExceptionReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
