package com.acme.transport.lastmile.locker.lockerassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveLockerAgreementStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveLockerAgreementStore()
        val item = newAdaptiveLockerAgreement("code-710", "name-889")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
