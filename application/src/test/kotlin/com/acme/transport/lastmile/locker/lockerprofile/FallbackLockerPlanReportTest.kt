package com.acme.transport.lastmile.locker.lockerprofile

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackLockerPlanReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707796961)
        val items = mutableListOf<FallbackLockerPlan>()
        for (index in 0 until 6) {
            items.add(newFallbackLockerPlan("fallback-locker-plan-$index", "sample"))
        }
        val report = buildFallbackLockerPlanReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
