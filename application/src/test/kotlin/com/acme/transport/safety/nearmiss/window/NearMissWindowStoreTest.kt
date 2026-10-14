package com.acme.transport.safety.nearmiss.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class NearMissWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryNearMissWindowStore()
        val item = newNearMissWindow("code-970", "title-5")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
