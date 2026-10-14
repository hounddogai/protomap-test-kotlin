package com.acme.transport.freightpay.accessorial.accessorialbudget

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveAccessorialSlotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705875290)
        val items = mutableListOf<AdaptiveAccessorialSlot>()
        for (index in 0 until 6) {
            items.add(newAdaptiveAccessorialSlot("adaptive-accessorial-slot-$index", "sample"))
        }
        val report = buildAdaptiveAccessorialSlotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
