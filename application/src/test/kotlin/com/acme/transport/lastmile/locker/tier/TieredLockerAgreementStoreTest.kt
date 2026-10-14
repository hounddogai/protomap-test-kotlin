package com.acme.transport.lastmile.locker.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredLockerAgreementStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredLockerAgreementStore()
        val item = newTieredLockerAgreement("code-208", "title-389")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
