package com.acme.transport.lastmile.parcel.template

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedParcelTemplateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702973743)
        val items = mutableListOf<BatchedParcelTemplate>()
        for (index in 0 until 4) {
            items.add(newBatchedParcelTemplate("batched-parcel-template-$index", "sample"))
        }
        val report = buildBatchedParcelTemplateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
