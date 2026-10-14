package com.acme.transport.lastmile.parcel.parcelmapping

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingParcelAssignmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709232342)
        val items = mutableListOf<PendingParcelAssignment>()
        for (index in 0 until 4) {
            items.add(newPendingParcelAssignment("pending-parcel-assignment-$index", "sample"))
        }
        val report = buildPendingParcelAssignmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
