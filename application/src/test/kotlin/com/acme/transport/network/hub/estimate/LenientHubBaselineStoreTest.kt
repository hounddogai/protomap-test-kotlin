package com.acme.transport.network.hub.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientHubBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientHubBaselineStore()
        val item = newLenientHubBaseline("code-415", "name-315")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
