package com.acme.transport.safety.nearmiss.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class NearMissPreferenceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryNearMissPreferenceStore()
        val item = newNearMissPreference("code-185", "label-881")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
