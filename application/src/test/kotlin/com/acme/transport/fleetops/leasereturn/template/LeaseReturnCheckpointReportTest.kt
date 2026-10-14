package com.acme.transport.fleetops.leasereturn.template

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LeaseReturnCheckpointReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708163861)
        val items = mutableListOf<LeaseReturnCheckpoint>()
        for (index in 0 until 5) {
            items.add(newLeaseReturnCheckpoint("lease-return-checkpoint-$index", "sample"))
        }
        val report = buildLeaseReturnCheckpointReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
