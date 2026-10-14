package com.acme.transport.network.cutoff.cutoffclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultCutoffClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultCutoffClaimStore()
        val item = newDefaultCutoffClaim("code-71", "name-232")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
