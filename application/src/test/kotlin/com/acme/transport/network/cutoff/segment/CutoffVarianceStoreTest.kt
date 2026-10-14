package com.acme.transport.network.cutoff.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CutoffVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCutoffVarianceStore()
        val item = newCutoffVariance("code-364", "name-394")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
