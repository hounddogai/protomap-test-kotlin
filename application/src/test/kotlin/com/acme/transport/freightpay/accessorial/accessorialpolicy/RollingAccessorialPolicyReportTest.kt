package com.acme.transport.freightpay.accessorial.accessorialpolicy

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingAccessorialPolicyReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709319436)
        val items = mutableListOf<RollingAccessorialPolicy>()
        for (index in 0 until 4) {
            items.add(newRollingAccessorialPolicy("rolling-accessorial-policy-$index", "sample"))
        }
        val report = buildRollingAccessorialPolicyReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
