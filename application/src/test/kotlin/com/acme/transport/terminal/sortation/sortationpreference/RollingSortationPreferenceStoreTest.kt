package com.acme.transport.terminal.sortation.sortationpreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingSortationPreferenceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingSortationPreferenceStore()
        val item = newRollingSortationPreference("code-94", "title-75")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
