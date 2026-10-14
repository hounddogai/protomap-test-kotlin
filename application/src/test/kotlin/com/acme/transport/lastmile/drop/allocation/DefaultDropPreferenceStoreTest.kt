package com.acme.transport.lastmile.drop.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultDropPreferenceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultDropPreferenceStore()
        val item = newDefaultDropPreference("code-88", "title-315")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
