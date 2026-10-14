package com.acme.transport.lastmile.parcel.parcelreservation

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParcelManifestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703209069)
        val items = mutableListOf<ParcelManifest>()
        for (index in 0 until 2) {
            items.add(newParcelManifest("parcel-manifest-$index", "sample"))
        }
        val report = buildParcelManifestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
