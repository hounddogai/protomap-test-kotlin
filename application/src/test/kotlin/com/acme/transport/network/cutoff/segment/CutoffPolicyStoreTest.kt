package com.acme.transport.network.cutoff.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CutoffPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCutoffPolicyStore()
        val item = newCutoffPolicy("code-86", "title-456")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
