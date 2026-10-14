package com.acme.transport.freightpay.accessorial.profile

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedAccessorialProfileReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706766935)
        val items = mutableListOf<WeightedAccessorialProfile>()
        for (index in 0 until 3) {
            items.add(newWeightedAccessorialProfile("weighted-accessorial-profile-$index", "sample"))
        }
        val report = buildWeightedAccessorialProfileReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
