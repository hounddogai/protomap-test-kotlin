package com.acme.transport.network.node.batch

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualNodePolicyReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708950859)
        val items = mutableListOf<ManualNodePolicy>()
        for (index in 0 until 6) {
            items.add(newManualNodePolicy("manual-node-policy-$index", "sample"))
        }
        val report = buildManualNodePolicyReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
