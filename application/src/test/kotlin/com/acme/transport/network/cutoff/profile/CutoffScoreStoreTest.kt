package com.acme.transport.network.cutoff.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CutoffScoreStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCutoffScoreStore()
        val item = newCutoffScore("code-614", "title-923")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
