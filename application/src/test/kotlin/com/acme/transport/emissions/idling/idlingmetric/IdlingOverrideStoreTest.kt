package com.acme.transport.emissions.idling.idlingmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class IdlingOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryIdlingOverrideStore()
        val item = newIdlingOverride("code-224", "title-643")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
