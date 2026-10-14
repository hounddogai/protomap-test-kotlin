package com.acme.transport.network.zone.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedZoneClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedZoneClaimStore()
        val item = newCappedZoneClaim("code-767", "name-127")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
