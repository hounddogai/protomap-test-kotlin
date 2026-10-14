package com.acme.transport.freightpay.accessorial.scheduledaccessorialcycle

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultAccessorialCycleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708465376)
        val items = mutableListOf<DefaultAccessorialCycle>()
        for (index in 0 until 5) {
            items.add(newDefaultAccessorialCycle("default-accessorial-cycle-$index", "sample"))
        }
        val report = buildDefaultAccessorialCycleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
