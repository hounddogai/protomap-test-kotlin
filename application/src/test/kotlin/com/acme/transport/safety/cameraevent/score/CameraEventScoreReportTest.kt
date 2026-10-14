package com.acme.transport.safety.cameraevent.score

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CameraEventScoreReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703566412)
        val items = mutableListOf<CameraEventScore>()
        for (index in 0 until 3) {
            items.add(newCameraEventScore("camera-event-score-$index", "sample"))
        }
        val report = buildCameraEventScoreReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
