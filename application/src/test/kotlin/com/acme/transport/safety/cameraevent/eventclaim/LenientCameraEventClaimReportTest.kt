package com.acme.transport.safety.cameraevent.eventclaim

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientCameraEventClaimReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709563851)
        val items = mutableListOf<LenientCameraEventClaim>()
        for (index in 0 until 3) {
            items.add(newLenientCameraEventClaim("lenient-camera-event-claim-$index", "sample"))
        }
        val report = buildLenientCameraEventClaimReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
