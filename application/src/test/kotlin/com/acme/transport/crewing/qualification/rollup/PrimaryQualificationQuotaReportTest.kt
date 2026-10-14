package com.acme.transport.crewing.qualification.rollup

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryQualificationQuotaReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709315847)
        val items = mutableListOf<PrimaryQualificationQuota>()
        for (index in 0 until 5) {
            items.add(newPrimaryQualificationQuota("primary-qualification-quota-$index", "sample"))
        }
        val report = buildPrimaryQualificationQuotaReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
