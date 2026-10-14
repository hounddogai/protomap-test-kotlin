package com.acme.transport.crewing.qualification.qualificationoverride

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveQualificationOverrideReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700757453)
        val items = mutableListOf<AdaptiveQualificationOverride>()
        for (index in 0 until 2) {
            items.add(newAdaptiveQualificationOverride("adaptive-qualification-override-$index", "sample"))
        }
        val report = buildAdaptiveQualificationOverrideReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
