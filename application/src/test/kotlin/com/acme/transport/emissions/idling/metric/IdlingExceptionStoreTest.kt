package com.acme.transport.emissions.idling.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class IdlingExceptionStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryIdlingExceptionStore()
        val item = newIdlingException("code-610", "label-491")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
