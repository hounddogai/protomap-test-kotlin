package com.acme.transport.network.hub.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedHubBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedHubBaselineStore()
        val item = newCappedHubBaseline("code-324", "label-393")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
