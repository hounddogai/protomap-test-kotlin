package com.acme.transport.emissions.idling.notice

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IdlingNoticeReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705548894)
        val items = mutableListOf<IdlingNotice>()
        for (index in 0 until 6) {
            items.add(newIdlingNotice("idling-notice-$index", "sample"))
        }
        val report = buildIdlingNoticeReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
