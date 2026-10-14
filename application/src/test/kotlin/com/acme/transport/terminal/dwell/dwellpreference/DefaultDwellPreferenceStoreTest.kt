package com.acme.transport.terminal.dwell.dwellpreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultDwellPreferenceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultDwellPreferenceStore()
        val item = newDefaultDwellPreference("code-276", "name-114")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
