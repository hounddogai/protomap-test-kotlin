package com.acme.transport.network.servicearea.weightedareaclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientServiceAreaScoreStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientServiceAreaScoreStore()
        val item = newLenientServiceAreaScore("code-913", "title-448")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
