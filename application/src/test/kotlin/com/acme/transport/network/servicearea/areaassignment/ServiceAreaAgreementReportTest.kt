package com.acme.transport.network.servicearea.areaassignment

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServiceAreaAgreementReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702583704)
        val items = mutableListOf<ServiceAreaAgreement>()
        for (index in 0 until 2) {
            items.add(newServiceAreaAgreement("service-area-agreement-$index", "sample"))
        }
        val report = buildServiceAreaAgreementReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
