package com.acme.transport.linehaul.laneplan.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LanePlanOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLanePlanOverrideStore()
        val item = newLanePlanOverride("code-230", "label-21")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
