package com.acme.transport.emissions.idling.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedIdlingWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedIdlingWindowStore()
        val item = newCappedIdlingWindow("code-175", "title-812")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
