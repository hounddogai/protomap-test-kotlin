package com.acme.transport.network.hub.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualHubBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualHubBaselineStore()
        val item = newManualHubBaseline("code-767", "title-517")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
