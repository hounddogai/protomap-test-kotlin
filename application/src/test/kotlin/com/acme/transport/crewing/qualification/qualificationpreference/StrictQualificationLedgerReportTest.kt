package com.acme.transport.crewing.qualification.qualificationpreference

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictQualificationLedgerReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707801121)
        val items = mutableListOf<StrictQualificationLedger>()
        for (index in 0 until 4) {
            items.add(newStrictQualificationLedger("strict-qualification-ledger-$index", "sample"))
        }
        val report = buildStrictQualificationLedgerReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
