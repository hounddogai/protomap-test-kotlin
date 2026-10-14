package com.acme.transport.emissions.emissionfactor.draft

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmissionFactorDraftReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703696483)
        val items = mutableListOf<EmissionFactorDraft>()
        for (index in 0 until 4) {
            items.add(newEmissionFactorDraft("emission-factor-draft-$index", "sample"))
        }
        val report = buildEmissionFactorDraftReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
