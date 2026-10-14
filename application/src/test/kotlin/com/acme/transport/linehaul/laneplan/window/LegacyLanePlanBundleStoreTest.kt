package com.acme.transport.linehaul.laneplan.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyLanePlanBundleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyLanePlanBundleStore()
        val item = newLegacyLanePlanBundle("code-73", "title-344")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
