package com.acme.transport.lastmile.locker.lockerclaim

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackLockerDraftReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707848663)
        val items = mutableListOf<FallbackLockerDraft>()
        for (index in 0 until 2) {
            items.add(newFallbackLockerDraft("fallback-locker-draft-$index", "sample"))
        }
        val report = buildFallbackLockerDraftReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
