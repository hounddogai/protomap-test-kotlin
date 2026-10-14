package com.acme.transport.network.zone.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ZoneAgreementStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryZoneAgreementStore()
        val item = newZoneAgreement("code-956", "title-280")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
