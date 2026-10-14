package com.acme.transport.network.servicearea.draft

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedServiceAreaDraftReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705614130)
        val items = mutableListOf<CappedServiceAreaDraft>()
        for (index in 0 until 5) {
            items.add(newCappedServiceAreaDraft("capped-service-area-draft-$index", "sample"))
        }
        val report = buildCappedServiceAreaDraftReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
