package com.acme.transport.linehaul.laneplan.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LanePlanRollupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLanePlanRollupStore()
        val item = newLanePlanRollup("code-103", "title-861")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
