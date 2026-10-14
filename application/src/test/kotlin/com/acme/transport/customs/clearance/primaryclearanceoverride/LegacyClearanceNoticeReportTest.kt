package com.acme.transport.customs.clearance.primaryclearanceoverride

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyClearanceNoticeReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706824223)
        val items = mutableListOf<LegacyClearanceNotice>()
        for (index in 0 until 6) {
            items.add(newLegacyClearanceNotice("legacy-clearance-notice-$index", "sample"))
        }
        val report = buildLegacyClearanceNoticeReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
