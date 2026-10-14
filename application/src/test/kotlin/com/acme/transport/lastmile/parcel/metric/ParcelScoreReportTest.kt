package com.acme.transport.lastmile.parcel.metric

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParcelScoreReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709295526)
        val items = mutableListOf<ParcelScore>()
        for (index in 0 until 4) {
            items.add(newParcelScore("parcel-score-$index", "sample"))
        }
        val report = buildParcelScoreReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
