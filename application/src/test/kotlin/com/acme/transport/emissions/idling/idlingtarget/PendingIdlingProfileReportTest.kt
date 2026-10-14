package com.acme.transport.emissions.idling.idlingtarget

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingIdlingProfileReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702421056)
        val items = mutableListOf<PendingIdlingProfile>()
        for (index in 0 until 3) {
            items.add(newPendingIdlingProfile("pending-idling-profile-$index", "sample"))
        }
        val report = buildPendingIdlingProfileReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
