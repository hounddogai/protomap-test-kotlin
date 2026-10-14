package com.acme.transport.terminal.traileryard.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalTrailerYardPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalTrailerYardPolicyStore()
        val item = newGlobalTrailerYardPolicy("code-513", "label-819")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
