package com.acme.transport.network.cutoff.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedCutoffTargetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedCutoffTargetStore()
        val item = newCappedCutoffTarget("code-284", "label-739")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
