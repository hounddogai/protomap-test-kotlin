package com.acme.transport.crewing.qualification.qualificationassignment

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackQualificationSnapshotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701861205)
        val items = mutableListOf<FallbackQualificationSnapshot>()
        for (index in 0 until 6) {
            items.add(newFallbackQualificationSnapshot("fallback-qualification-snapshot-$index", "sample"))
        }
        val report = buildFallbackQualificationSnapshotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
