package com.acme.transport.customs.clearance.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ClearanceRequestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryClearanceRequestStore()
        val item = newClearanceRequest("code-311", "name-7")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
