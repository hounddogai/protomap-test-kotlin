package com.acme.transport.terminal.dwell.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DwellPreferenceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDwellPreferenceStore()
        val item = newDwellPreference("code-192", "label-924")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
