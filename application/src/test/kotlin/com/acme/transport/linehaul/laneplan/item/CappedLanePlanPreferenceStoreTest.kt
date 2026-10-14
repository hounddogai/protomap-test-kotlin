package com.acme.transport.linehaul.laneplan.item

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedLanePlanPreferenceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedLanePlanPreferenceStore()
        val item = newCappedLanePlanPreference("code-836", "title-761")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
