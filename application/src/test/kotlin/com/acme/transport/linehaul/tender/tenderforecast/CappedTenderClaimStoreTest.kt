package com.acme.transport.linehaul.tender.tenderforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedTenderClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedTenderClaimStore()
        val item = newCappedTenderClaim("code-308", "name-936")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
