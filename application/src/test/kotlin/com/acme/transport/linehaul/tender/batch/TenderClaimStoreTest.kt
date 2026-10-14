package com.acme.transport.linehaul.tender.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TenderClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTenderClaimStore()
        val item = newTenderClaim("code-48", "name-63")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
