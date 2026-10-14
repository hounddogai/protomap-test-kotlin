package com.acme.transport.linehaul.laneplan.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LanePlanExceptionStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLanePlanExceptionStore()
        val item = newLanePlanException("code-444", "title-372")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
