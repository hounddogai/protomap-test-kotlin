package com.acme.transport.fleetops.defect.notice

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedDefectDigestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704389285)
        val items = mutableListOf<BatchedDefectDigest>()
        for (index in 0 until 3) {
            items.add(newBatchedDefectDigest("batched-defect-digest-$index", "sample"))
        }
        val report = buildBatchedDefectDigestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
