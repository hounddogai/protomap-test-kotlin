package com.acme.transport.network.servicearea.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ServiceAreaOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryServiceAreaOverrideStore()
        val item = newServiceAreaOverride("code-939", "title-685")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
