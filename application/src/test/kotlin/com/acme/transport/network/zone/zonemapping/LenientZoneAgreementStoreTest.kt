package com.acme.transport.network.zone.zonemapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientZoneAgreementStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientZoneAgreementStore()
        val item = newLenientZoneAgreement("code-530", "label-18")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
