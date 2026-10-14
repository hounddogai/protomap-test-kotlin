package com.acme.transport.safety.cameraevent.defaulteventscore

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredCameraEventScoreReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705426367)
        val items = mutableListOf<TieredCameraEventScore>()
        for (index in 0 until 2) {
            items.add(newTieredCameraEventScore("tiered-camera-event-score-$index", "sample"))
        }
        val report = buildTieredCameraEventScoreReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
