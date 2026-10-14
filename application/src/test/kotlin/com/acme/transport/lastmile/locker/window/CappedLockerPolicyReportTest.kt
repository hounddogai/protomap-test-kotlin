package com.acme.transport.lastmile.locker.window

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedLockerPolicyReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702844220)
        val items = mutableListOf<CappedLockerPolicy>()
        for (index in 0 until 6) {
            items.add(newCappedLockerPolicy("capped-locker-policy-$index", "sample"))
        }
        val report = buildCappedLockerPolicyReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
