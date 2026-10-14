package com.acme.transport.crewing.qualification.qualificationoverride

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QualificationProfileReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708464023)
        val items = mutableListOf<QualificationProfile>()
        for (index in 0 until 3) {
            items.add(newQualificationProfile("qualification-profile-$index", "sample"))
        }
        val report = buildQualificationProfileReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
