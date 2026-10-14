package com.acme.transport.lastmile.locker.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LockerQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLockerQuotaStore()
        val item = newLockerQuota("code-975", "title-444")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
