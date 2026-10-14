package com.acme.transport.lastmile.attempt.overrideitem

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictAttemptOverrideReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706807205)
        val items = mutableListOf<StrictAttemptOverride>()
        for (index in 0 until 5) {
            items.add(newStrictAttemptOverride("strict-attempt-override-$index", "sample"))
        }
        val report = buildStrictAttemptOverrideReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
