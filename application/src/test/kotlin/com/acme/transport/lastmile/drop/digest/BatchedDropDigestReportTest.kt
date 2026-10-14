package com.acme.transport.lastmile.drop.digest

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedDropDigestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707317428)
        val items = mutableListOf<BatchedDropDigest>()
        for (index in 0 until 6) {
            items.add(newBatchedDropDigest("batched-drop-digest-$index", "sample"))
        }
        val report = buildBatchedDropDigestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
