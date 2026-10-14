package com.acme.transport.freightpay.accessorial.scheduledaccessorialcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultAccessorialCycleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultAccessorialCycleStore()
        val item = newDefaultAccessorialCycle("code-693", "name-541")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
