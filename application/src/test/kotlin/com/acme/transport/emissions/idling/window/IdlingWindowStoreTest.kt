package com.acme.transport.emissions.idling.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class IdlingWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryIdlingWindowStore()
        val item = newIdlingWindow("code-520", "title-166")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
