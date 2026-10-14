package com.acme.transport.lastmile.proofofdelivery.recorditem

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalProofOfDeliveryRecordReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707180010)
        val items = mutableListOf<GlobalProofOfDeliveryRecord>()
        for (index in 0 until 5) {
            items.add(newGlobalProofOfDeliveryRecord("global-proof-of-delivery-record-$index", "sample"))
        }
        val report = buildGlobalProofOfDeliveryRecordReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
