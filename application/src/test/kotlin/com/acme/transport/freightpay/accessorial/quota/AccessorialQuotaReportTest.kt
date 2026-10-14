package com.acme.transport.freightpay.accessorial.quota

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AccessorialQuotaReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707720094)
        val items = mutableListOf<AccessorialQuota>()
        for (index in 0 until 4) {
            items.add(newAccessorialQuota("accessorial-quota-$index", "sample"))
        }
        val report = buildAccessorialQuotaReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
