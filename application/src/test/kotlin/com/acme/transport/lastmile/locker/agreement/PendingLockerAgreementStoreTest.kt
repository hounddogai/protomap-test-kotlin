package com.acme.transport.lastmile.locker.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingLockerAgreementStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingLockerAgreementStore()
        val item = newPendingLockerAgreement("code-531", "title-290")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
