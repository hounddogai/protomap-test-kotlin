package com.acme.transport.network.transittime.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TransitTimeAgreementStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTransitTimeAgreementStore()
        val item = newTransitTimeAgreement("code-107", "label-301")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
