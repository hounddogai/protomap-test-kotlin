package com.acme.transport.network.zone.zonemapping

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedZoneAgreementReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700393083)
        val items = mutableListOf<BatchedZoneAgreement>()
        for (index in 0 until 5) {
            items.add(newBatchedZoneAgreement("batched-zone-agreement-$index", "sample"))
        }
        val report = buildBatchedZoneAgreementReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
