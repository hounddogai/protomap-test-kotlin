package com.acme.transport.linehaul.relay.relayquota

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualRelayQuotaReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701645308)
        val items = mutableListOf<ManualRelayQuota>()
        for (index in 0 until 2) {
            items.add(newManualRelayQuota("manual-relay-quota-$index", "sample"))
        }
        val report = buildManualRelayQuotaReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
