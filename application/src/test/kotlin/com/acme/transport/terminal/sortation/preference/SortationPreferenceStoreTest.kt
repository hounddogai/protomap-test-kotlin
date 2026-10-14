package com.acme.transport.terminal.sortation.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SortationPreferenceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySortationPreferenceStore()
        val item = newSortationPreference("code-859", "name-121")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
