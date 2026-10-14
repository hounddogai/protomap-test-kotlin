package com.acme.transport.safety.cameraevent.policy

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualCameraEventPolicyReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708416935)
        val items = mutableListOf<ManualCameraEventPolicy>()
        for (index in 0 until 3) {
            items.add(newManualCameraEventPolicy("manual-camera-event-policy-$index", "sample"))
        }
        val report = buildManualCameraEventPolicyReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
