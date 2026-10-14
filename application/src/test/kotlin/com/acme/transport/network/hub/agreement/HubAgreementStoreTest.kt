package com.acme.transport.network.hub.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class HubAgreementStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryHubAgreementStore()
        val item = newHubAgreement("code-816", "label-266")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
