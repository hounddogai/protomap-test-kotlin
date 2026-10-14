package com.acme.transport.customs.clearance.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ClearanceClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryClearanceClaimStore()
        val item = newClearanceClaim("code-867", "label-978")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
