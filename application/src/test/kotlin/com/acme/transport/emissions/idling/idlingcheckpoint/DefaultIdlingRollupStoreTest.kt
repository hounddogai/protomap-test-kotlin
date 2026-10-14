package com.acme.transport.emissions.idling.idlingcheckpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultIdlingRollupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultIdlingRollupStore()
        val item = newDefaultIdlingRollup("code-450", "title-438")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
