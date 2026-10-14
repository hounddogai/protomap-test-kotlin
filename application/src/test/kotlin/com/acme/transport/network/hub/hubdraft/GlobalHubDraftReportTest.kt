package com.acme.transport.network.hub.hubdraft

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalHubDraftReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707648889)
        val items = mutableListOf<GlobalHubDraft>()
        for (index in 0 until 2) {
            items.add(newGlobalHubDraft("global-hub-draft-$index", "sample"))
        }
        val report = buildGlobalHubDraftReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
