package com.acme.transport.network.cutoff.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CutoffPreferenceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCutoffPreferenceStore()
        val item = newCutoffPreference("code-442", "title-77")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
