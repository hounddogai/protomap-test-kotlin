package com.acme.transport.network.servicearea.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ServiceAreaDraftStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryServiceAreaDraftStore()
        val item = newServiceAreaDraft("code-350", "name-322")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
