package com.acme.transport.fleetops.serviceplan.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualServicePlanSegmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualServicePlanSegmentStore()
        val item = newManualServicePlanSegment("code-91", "title-318")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
