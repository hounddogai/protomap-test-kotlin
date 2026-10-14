package com.acme.transport.network.cutoff.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalCutoffPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalCutoffPolicyStore()
        val item = newGlobalCutoffPolicy("code-760", "label-609")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
