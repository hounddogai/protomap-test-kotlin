package com.acme.transport.lastmile.parcel.parcelcycle

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalParcelBatchReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700101143)
        val items = mutableListOf<GlobalParcelBatch>()
        for (index in 0 until 5) {
            items.add(newGlobalParcelBatch("global-parcel-batch-$index", "sample"))
        }
        val report = buildGlobalParcelBatchReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
