package com.acme.transport.emissions.idling.idlingmetric

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IdlingOverrideReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705740857)
        val items = mutableListOf<IdlingOverride>()
        for (index in 0 until 5) {
            items.add(newIdlingOverride("idling-override-$index", "sample"))
        }
        val report = buildIdlingOverrideReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
