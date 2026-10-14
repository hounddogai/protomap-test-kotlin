package com.acme.transport.lastmile.locker.lockerclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackLockerDraftStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackLockerDraftStore()
        val item = newFallbackLockerDraft("code-184", "title-624")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
