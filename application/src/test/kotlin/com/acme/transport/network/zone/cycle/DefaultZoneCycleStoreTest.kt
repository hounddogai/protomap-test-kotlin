package com.acme.transport.network.zone.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultZoneCycleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultZoneCycleStore()
        val item = newDefaultZoneCycle("code-802", "title-910")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
