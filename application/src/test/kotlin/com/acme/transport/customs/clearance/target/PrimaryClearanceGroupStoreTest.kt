package com.acme.transport.customs.clearance.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryClearanceGroupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryClearanceGroupStore()
        val item = newPrimaryClearanceGroup("code-422", "name-751")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
