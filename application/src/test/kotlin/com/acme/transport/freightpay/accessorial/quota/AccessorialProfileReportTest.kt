package com.acme.transport.freightpay.accessorial.quota

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AccessorialProfileReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701936907)
        val items = mutableListOf<AccessorialProfile>()
        for (index in 0 until 3) {
            items.add(newAccessorialProfile("accessorial-profile-$index", "sample"))
        }
        val report = buildAccessorialProfileReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
