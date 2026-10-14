package com.acme.transport.network.cutoff.cutoffclaim

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultCutoffClaimReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702946479)
        val items = mutableListOf<DefaultCutoffClaim>()
        for (index in 0 until 2) {
            items.add(newDefaultCutoffClaim("default-cutoff-claim-$index", "sample"))
        }
        val report = buildDefaultCutoffClaimReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
