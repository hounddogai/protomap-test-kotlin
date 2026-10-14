package com.acme.transport.network.cutoff.weightedcutoffentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CutoffOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCutoffOverrideStore()
        val item = newCutoffOverride("code-351", "label-829")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
