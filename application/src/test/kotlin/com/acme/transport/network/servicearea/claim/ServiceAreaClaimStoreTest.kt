package com.acme.transport.network.servicearea.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ServiceAreaClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryServiceAreaClaimStore()
        val item = newServiceAreaClaim("code-135", "title-601")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
