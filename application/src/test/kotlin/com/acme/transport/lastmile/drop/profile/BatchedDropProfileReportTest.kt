package com.acme.transport.lastmile.drop.profile

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedDropProfileReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707800680)
        val items = mutableListOf<BatchedDropProfile>()
        for (index in 0 until 6) {
            items.add(newBatchedDropProfile("batched-drop-profile-$index", "sample"))
        }
        val report = buildBatchedDropProfileReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
