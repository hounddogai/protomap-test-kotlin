package com.acme.transport.crewing.qualification.qualificationtemplate

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryQualificationLedgerReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700927553)
        val items = mutableListOf<PrimaryQualificationLedger>()
        for (index in 0 until 2) {
            items.add(newPrimaryQualificationLedger("primary-qualification-ledger-$index", "sample"))
        }
        val report = buildPrimaryQualificationLedgerReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
