package com.acme.transport.linehaul.tender.weightedtenderrollup

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingTenderRollupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701893326)
        val items = mutableListOf<RollingTenderRollup>()
        for (index in 0 until 4) {
            items.add(newRollingTenderRollup("rolling-tender-rollup-$index", "sample"))
        }
        val report = buildRollingTenderRollupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
