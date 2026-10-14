package com.acme.transport.linehaul.laneplan.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryLanePlanRequestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryLanePlanRequestStore()
        val item = newPrimaryLanePlanRequest("code-844", "name-636")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
