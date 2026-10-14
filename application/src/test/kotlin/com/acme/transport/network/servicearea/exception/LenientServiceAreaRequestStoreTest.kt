package com.acme.transport.network.servicearea.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientServiceAreaRequestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientServiceAreaRequestStore()
        val item = newLenientServiceAreaRequest("code-387", "label-744")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
