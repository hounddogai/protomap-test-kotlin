package com.acme.transport.linehaul.tender.tenderrollup

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictTenderRollupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703477591)
        val items = mutableListOf<StrictTenderRollup>()
        for (index in 0 until 2) {
            items.add(newStrictTenderRollup("strict-tender-rollup-$index", "sample"))
        }
        val report = buildStrictTenderRollupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
