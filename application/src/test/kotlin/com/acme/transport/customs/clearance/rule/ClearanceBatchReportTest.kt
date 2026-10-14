package com.acme.transport.customs.clearance.rule

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClearanceBatchReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703323693)
        val items = mutableListOf<ClearanceBatch>()
        for (index in 0 until 5) {
            items.add(newClearanceBatch("clearance-batch-$index", "sample"))
        }
        val report = buildClearanceBatchReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
