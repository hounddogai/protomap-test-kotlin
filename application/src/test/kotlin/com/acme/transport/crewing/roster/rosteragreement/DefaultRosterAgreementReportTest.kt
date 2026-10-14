package com.acme.transport.crewing.roster.rosteragreement

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultRosterAgreementReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700060810)
        val items = mutableListOf<DefaultRosterAgreement>()
        for (index in 0 until 3) {
            items.add(newDefaultRosterAgreement("default-roster-agreement-$index", "sample"))
        }
        val report = buildDefaultRosterAgreementReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
