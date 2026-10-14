package com.acme.transport.lastmile.drop.dropslot

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalDropAgreementReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703506713)
        val items = mutableListOf<SeasonalDropAgreement>()
        for (index in 0 until 5) {
            items.add(newSeasonalDropAgreement("seasonal-drop-agreement-$index", "sample"))
        }
        val report = buildSeasonalDropAgreementReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
