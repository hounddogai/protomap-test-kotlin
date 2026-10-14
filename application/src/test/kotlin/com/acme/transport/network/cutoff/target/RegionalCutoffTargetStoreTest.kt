package com.acme.transport.network.cutoff.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalCutoffTargetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalCutoffTargetStore()
        val item = newRegionalCutoffTarget("code-381", "name-62")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
