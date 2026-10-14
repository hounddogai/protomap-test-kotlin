package com.acme.transport.safety.nearmiss.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class NearMissClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryNearMissClaimStore()
        val item = newNearMissClaim("code-601", "title-238")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
