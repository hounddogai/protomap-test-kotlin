package com.acme.transport.customs.clearance.clearanceoverride

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ClearanceProfileStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryClearanceProfileStore()
        val item = newClearanceProfile("code-457", "label-199")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
