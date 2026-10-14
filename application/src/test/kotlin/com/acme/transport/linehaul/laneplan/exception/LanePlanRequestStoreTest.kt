package com.acme.transport.linehaul.laneplan.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LanePlanRequestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLanePlanRequestStore()
        val item = newLanePlanRequest("code-125", "label-467")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
