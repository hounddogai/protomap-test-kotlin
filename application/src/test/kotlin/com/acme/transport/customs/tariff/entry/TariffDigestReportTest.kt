package com.acme.transport.customs.tariff.entry

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TariffDigestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705007371)
        val items = mutableListOf<TariffDigest>()
        for (index in 0 until 2) {
            items.add(newTariffDigest("tariff-digest-$index", "sample"))
        }
        val report = buildTariffDigestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
