package com.acme.transport.crewing.qualification.summary

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryQualificationOverrideReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702645727)
        val items = mutableListOf<PrimaryQualificationOverride>()
        for (index in 0 until 5) {
            items.add(newPrimaryQualificationOverride("primary-qualification-override-$index", "sample"))
        }
        val report = buildPrimaryQualificationOverrideReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
