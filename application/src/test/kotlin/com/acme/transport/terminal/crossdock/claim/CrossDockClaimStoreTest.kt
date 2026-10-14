package com.acme.transport.terminal.crossdock.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CrossDockClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCrossDockClaimStore()
        val item = newCrossDockClaim("code-246", "title-582")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
