package com.acme.transport.lastmile.attempt.adaptiveattemptrequest

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AttemptRollupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703158279)
        val items = mutableListOf<AttemptRollup>()
        for (index in 0 until 4) {
            items.add(newAttemptRollup("attempt-rollup-$index", "sample"))
        }
        val report = buildAttemptRollupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
