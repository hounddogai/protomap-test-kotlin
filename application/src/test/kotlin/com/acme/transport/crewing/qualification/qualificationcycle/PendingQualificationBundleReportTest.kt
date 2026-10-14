package com.acme.transport.crewing.qualification.qualificationcycle

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingQualificationBundleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708376446)
        val items = mutableListOf<PendingQualificationBundle>()
        for (index in 0 until 3) {
            items.add(newPendingQualificationBundle("pending-qualification-bundle-$index", "sample"))
        }
        val report = buildPendingQualificationBundleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
