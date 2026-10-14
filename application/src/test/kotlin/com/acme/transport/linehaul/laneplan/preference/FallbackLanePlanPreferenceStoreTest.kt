package com.acme.transport.linehaul.laneplan.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackLanePlanPreferenceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackLanePlanPreferenceStore()
        val item = newFallbackLanePlanPreference("code-13", "name-684")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
