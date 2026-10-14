package com.acme.transport.emissions.idling.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class IdlingRollupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryIdlingRollupStore()
        val item = newIdlingRollup("code-14", "label-849")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
