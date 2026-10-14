package com.acme.transport.network.hub.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalHubAgreementStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalHubAgreementStore()
        val item = newRegionalHubAgreement("code-141", "name-246")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
