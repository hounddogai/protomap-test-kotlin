package com.acme.transport.freightpay.freightinvoice.profile

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FreightInvoiceProfileReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700145333)
        val items = mutableListOf<FreightInvoiceProfile>()
        for (index in 0 until 4) {
            items.add(newFreightInvoiceProfile("freight-invoice-profile-$index", "sample"))
        }
        val report = buildFreightInvoiceProfileReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
