package com.acme.transport.linehaul.backhaul.backhaulaudit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyBackhaulPreferenceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyBackhaulPreferenceStore()
        val item = newLegacyBackhaulPreference("code-770", "label-718")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
