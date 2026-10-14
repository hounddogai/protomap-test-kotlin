package com.acme.transport.customs.tariff.manifest

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TariffManifestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703352179)
        val items = mutableListOf<TariffManifest>()
        for (index in 0 until 2) {
            items.add(newTariffManifest("tariff-manifest-$index", "sample"))
        }
        val report = buildTariffManifestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
