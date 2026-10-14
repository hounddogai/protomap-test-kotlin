package com.acme.transport.lastmile.locker.tier

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredLockerAgreementReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706308743)
        val items = mutableListOf<TieredLockerAgreement>()
        for (index in 0 until 6) {
            items.add(newTieredLockerAgreement("tiered-locker-agreement-$index", "sample"))
        }
        val report = buildTieredLockerAgreementReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
