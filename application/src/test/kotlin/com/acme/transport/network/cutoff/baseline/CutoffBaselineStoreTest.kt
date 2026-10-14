package com.acme.transport.network.cutoff.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CutoffBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCutoffBaselineStore()
        val item = newCutoffBaseline("code-639", "label-288")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
