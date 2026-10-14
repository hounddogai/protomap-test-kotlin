package com.acme.transport.freightpay.freightinvoice.quota

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalFreightInvoiceManifestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709036076)
        val items = mutableListOf<SeasonalFreightInvoiceManifest>()
        for (index in 0 until 3) {
            items.add(newSeasonalFreightInvoiceManifest("seasonal-freight-invoice-manifest-$index", "sample"))
        }
        val report = buildSeasonalFreightInvoiceManifestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
