package com.acme.transport.network.cutoff.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualCutoffClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualCutoffClaimStore()
        val item = newManualCutoffClaim("code-894", "label-92")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
