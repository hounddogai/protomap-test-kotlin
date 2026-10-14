package com.acme.transport.network.hub.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedHubAuditStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedHubAuditStore()
        val item = newWeightedHubAudit("code-621", "title-327")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
