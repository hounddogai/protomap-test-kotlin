package com.acme.transport.safety.cameraevent.adjustment

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CameraEventAgreementReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705802034)
        val items = mutableListOf<CameraEventAgreement>()
        for (index in 0 until 6) {
            items.add(newCameraEventAgreement("camera-event-agreement-$index", "sample"))
        }
        val report = buildCameraEventAgreementReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
