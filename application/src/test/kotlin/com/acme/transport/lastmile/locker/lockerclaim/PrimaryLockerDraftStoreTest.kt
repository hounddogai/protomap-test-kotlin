package com.acme.transport.lastmile.locker.lockerclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryLockerDraftStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryLockerDraftStore()
        val item = newPrimaryLockerDraft("code-913", "title-851")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
