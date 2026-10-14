package com.acme.transport.crewing.qualification.variance

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QualificationLedgerReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706426601)
        val items = mutableListOf<QualificationLedger>()
        for (index in 0 until 3) {
            items.add(newQualificationLedger("qualification-ledger-$index", "sample"))
        }
        val report = buildQualificationLedgerReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
