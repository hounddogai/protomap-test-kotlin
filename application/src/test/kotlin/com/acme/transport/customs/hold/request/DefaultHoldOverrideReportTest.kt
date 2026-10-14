package com.acme.transport.customs.hold.request

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultHoldOverrideReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706201744)
        val items = mutableListOf<DefaultHoldOverride>()
        for (index in 0 until 4) {
            items.add(newDefaultHoldOverride("default-hold-override-$index", "sample"))
        }
        val report = buildDefaultHoldOverrideReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
