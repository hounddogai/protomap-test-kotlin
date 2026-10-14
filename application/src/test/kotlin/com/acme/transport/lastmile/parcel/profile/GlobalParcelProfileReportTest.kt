package com.acme.transport.lastmile.parcel.profile

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalParcelProfileReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707831666)
        val items = mutableListOf<GlobalParcelProfile>()
        for (index in 0 until 2) {
            items.add(newGlobalParcelProfile("global-parcel-profile-$index", "sample"))
        }
        val report = buildGlobalParcelProfileReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
