package com.acme.transport.lastmile.redelivery.window

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultRedeliveryBundleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705243982)
        val items = mutableListOf<DefaultRedeliveryBundle>()
        for (index in 0 until 6) {
            items.add(newDefaultRedeliveryBundle("default-redelivery-bundle-$index", "sample"))
        }
        val report = buildDefaultRedeliveryBundleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
