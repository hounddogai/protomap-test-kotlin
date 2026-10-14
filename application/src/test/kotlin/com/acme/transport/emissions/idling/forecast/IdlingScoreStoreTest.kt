package com.acme.transport.emissions.idling.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class IdlingScoreStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryIdlingScoreStore()
        val item = newIdlingScore("code-277", "label-43")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
