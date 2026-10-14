package com.acme.transport.linehaul.laneplan.plansegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingLanePlanSegmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingLanePlanSegmentStore()
        val item = newPendingLanePlanSegment("code-717", "title-24")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
