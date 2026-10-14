package com.acme.transport.network.hub.primaryhubsummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalHubSnapshotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalHubSnapshotStore()
        val item = newRegionalHubSnapshot("code-878", "name-586")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
