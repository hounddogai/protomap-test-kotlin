package com.acme.transport.emissions.idling.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingIdlingRequestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingIdlingRequestStore()
        val item = newRollingIdlingRequest("code-135", "title-262")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
