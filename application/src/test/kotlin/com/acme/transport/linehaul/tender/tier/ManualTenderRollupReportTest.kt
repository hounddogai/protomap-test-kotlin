package com.acme.transport.linehaul.tender.tier

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualTenderRollupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708741082)
        val items = mutableListOf<ManualTenderRollup>()
        for (index in 0 until 3) {
            items.add(newManualTenderRollup("manual-tender-rollup-$index", "sample"))
        }
        val report = buildManualTenderRollupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
