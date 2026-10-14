package com.acme.transport.network.cutoff.cutoffentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalCutoffClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalCutoffClaimStore()
        val item = newRegionalCutoffClaim("code-515", "title-29")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
