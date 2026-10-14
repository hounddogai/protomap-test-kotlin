package com.acme.transport.customs.clearance.clearanceoverride

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ClearancePolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryClearancePolicyStore()
        val item = newClearancePolicy("code-635", "name-638")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
