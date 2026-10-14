package com.acme.transport.emissions.idling.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualIdlingScoreStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualIdlingScoreStore()
        val item = newManualIdlingScore("code-802", "name-638")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
