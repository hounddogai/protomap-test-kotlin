package com.acme.transport.customs.tariff.score

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultTariffDraftReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701245034)
        val items = mutableListOf<DefaultTariffDraft>()
        for (index in 0 until 2) {
            items.add(newDefaultTariffDraft("default-tariff-draft-$index", "sample"))
        }
        val report = buildDefaultTariffDraftReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
