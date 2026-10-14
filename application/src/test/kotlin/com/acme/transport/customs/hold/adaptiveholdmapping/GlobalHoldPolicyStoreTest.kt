package com.acme.transport.customs.hold.adaptiveholdmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalHoldPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalHoldPolicyStore()
        val item = newGlobalHoldPolicy("code-562", "name-767")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
