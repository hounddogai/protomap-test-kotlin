package com.acme.transport.linehaul.trailer.trailerpreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalTrailerPreferenceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalTrailerPreferenceStore()
        val item = newRegionalTrailerPreference("code-825", "title-157")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
