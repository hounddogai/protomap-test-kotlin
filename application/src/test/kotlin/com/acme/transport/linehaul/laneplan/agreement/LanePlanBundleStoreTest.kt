package com.acme.transport.linehaul.laneplan.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LanePlanBundleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLanePlanBundleStore()
        val item = newLanePlanBundle("code-480", "name-168")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
