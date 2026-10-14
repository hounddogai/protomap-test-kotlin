package com.acme.transport.network.hub.hubsummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultHubTierStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultHubTierStore()
        val item = newDefaultHubTier("code-671", "title-321")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
