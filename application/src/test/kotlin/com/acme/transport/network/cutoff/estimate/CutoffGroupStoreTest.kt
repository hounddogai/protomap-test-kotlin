package com.acme.transport.network.cutoff.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CutoffGroupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCutoffGroupStore()
        val item = newCutoffGroup("code-922", "label-738")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
