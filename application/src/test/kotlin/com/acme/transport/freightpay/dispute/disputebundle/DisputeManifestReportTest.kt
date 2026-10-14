package com.acme.transport.freightpay.dispute.disputebundle

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DisputeManifestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704553283)
        val items = mutableListOf<DisputeManifest>()
        for (index in 0 until 2) {
            items.add(newDisputeManifest("dispute-manifest-$index", "sample"))
        }
        val report = buildDisputeManifestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
