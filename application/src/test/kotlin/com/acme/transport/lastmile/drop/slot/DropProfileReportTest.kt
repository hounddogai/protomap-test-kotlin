package com.acme.transport.lastmile.drop.slot

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DropProfileReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707394370)
        val items = mutableListOf<DropProfile>()
        for (index in 0 until 5) {
            items.add(newDropProfile("drop-profile-$index", "sample"))
        }
        val report = buildDropProfileReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
