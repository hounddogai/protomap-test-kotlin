package com.acme.transport.crewing.qualification.template

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedQualificationCycleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704658937)
        val items = mutableListOf<BatchedQualificationCycle>()
        for (index in 0 until 5) {
            items.add(newBatchedQualificationCycle("batched-qualification-cycle-$index", "sample"))
        }
        val report = buildBatchedQualificationCycleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
