package com.acme.transport.network.zone.zonetier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryZoneTierStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryZoneTierStore()
        val item = newPrimaryZoneTier("code-220", "title-124")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
