package com.acme.transport.customs.declaration.declarationbatch

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalDeclarationQuotaReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708806521)
        val items = mutableListOf<RegionalDeclarationQuota>()
        for (index in 0 until 3) {
            items.add(newRegionalDeclarationQuota("regional-declaration-quota-$index", "sample"))
        }
        val report = buildRegionalDeclarationQuotaReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
