package com.acme.transport.network.cutoff.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultCutoffEstimateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultCutoffEstimateStore()
        val item = newDefaultCutoffEstimate("code-656", "title-881")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
