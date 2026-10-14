package com.acme.transport.crewing.restbreak.regionalbreakpreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedRestBreakPreferenceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedRestBreakPreferenceStore()
        val item = newWeightedRestBreakPreference("code-111", "name-268")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
