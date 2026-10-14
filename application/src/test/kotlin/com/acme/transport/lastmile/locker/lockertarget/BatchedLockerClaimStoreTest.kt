package com.acme.transport.lastmile.locker.lockertarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedLockerClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedLockerClaimStore()
        val item = newBatchedLockerClaim("code-543", "name-245")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
