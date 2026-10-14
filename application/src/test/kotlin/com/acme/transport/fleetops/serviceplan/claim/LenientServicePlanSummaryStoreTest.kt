package com.acme.transport.fleetops.serviceplan.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientServicePlanSummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientServicePlanSummaryStore()
        val item = newLenientServicePlanSummary("code-709", "name-22")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
