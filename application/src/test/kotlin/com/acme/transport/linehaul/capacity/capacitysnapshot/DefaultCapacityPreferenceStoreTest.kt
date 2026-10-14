package com.acme.transport.linehaul.capacity.capacitysnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultCapacityPreferenceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultCapacityPreferenceStore()
        val item = newDefaultCapacityPreference("code-766", "title-981")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
