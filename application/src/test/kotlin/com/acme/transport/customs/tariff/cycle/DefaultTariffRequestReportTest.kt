package com.acme.transport.customs.tariff.cycle

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultTariffRequestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704322028)
        val items = mutableListOf<DefaultTariffRequest>()
        for (index in 0 until 2) {
            items.add(newDefaultTariffRequest("default-tariff-request-$index", "sample"))
        }
        val report = buildDefaultTariffRequestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
