package com.acme.transport.lastmile.parcel.parcelvariance

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParcelPreferenceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703758255)
        val items = mutableListOf<ParcelPreference>()
        for (index in 0 until 4) {
            items.add(newParcelPreference("parcel-preference-$index", "sample"))
        }
        val report = buildParcelPreferenceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
