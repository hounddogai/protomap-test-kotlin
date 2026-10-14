package com.acme.transport.customs.hold.adaptiveholdmapping

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalHoldPolicyReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703407783)
        val items = mutableListOf<GlobalHoldPolicy>()
        for (index in 0 until 2) {
            items.add(newGlobalHoldPolicy("global-hold-policy-$index", "sample"))
        }
        val report = buildGlobalHoldPolicyReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
