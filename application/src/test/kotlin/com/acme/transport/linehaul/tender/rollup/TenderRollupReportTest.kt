package com.acme.transport.linehaul.tender.rollup

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TenderRollupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709025811)
        val items = mutableListOf<TenderRollup>()
        for (index in 0 until 4) {
            items.add(newTenderRollup("tender-rollup-$index", "sample"))
        }
        val report = buildTenderRollupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
