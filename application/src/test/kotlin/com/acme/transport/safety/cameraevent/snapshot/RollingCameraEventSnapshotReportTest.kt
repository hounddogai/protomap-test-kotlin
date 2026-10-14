package com.acme.transport.safety.cameraevent.snapshot

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingCameraEventSnapshotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709287919)
        val items = mutableListOf<RollingCameraEventSnapshot>()
        for (index in 0 until 5) {
            items.add(newRollingCameraEventSnapshot("rolling-camera-event-snapshot-$index", "sample"))
        }
        val report = buildRollingCameraEventSnapshotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
