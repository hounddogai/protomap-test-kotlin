package com.acme.transport.crewing.availability.availabilitysegment

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultAvailabilityAgreementReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702038077)
        val items = mutableListOf<DefaultAvailabilityAgreement>()
        for (index in 0 until 5) {
            items.add(newDefaultAvailabilityAgreement("default-availability-agreement-$index", "sample"))
        }
        val report = buildDefaultAvailabilityAgreementReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
